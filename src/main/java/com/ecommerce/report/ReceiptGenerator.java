package com.ecommerce.report;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.User;
import com.ecommerce.ui.MainApp;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class ReceiptGenerator {

    public static void generateReceipt(Order order, String outputPath) {
        try {
            // 1. Build the temporary XML data file
            String xmlDataPath = "order_data_" + order.getId() + ".xml";
            generateXmlDataFile(order, xmlDataPath);

            // 2. Load and compile the JRXML template
            InputStream jrxmlStream = ReceiptGenerator.class
                    .getResourceAsStream("/reports/receipt.jrxml");
            if (jrxmlStream == null) {
                System.err.println("❌ receipt.jrxml not found in /reports/");
                return;
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

            // 3. Point Jasper at the XML file (XPath selects each <Item> as one data row)
            JRXmlDataSource xmlDataSource = new JRXmlDataSource(
                    new File(xmlDataPath), "/Order/Items/Item");

            // ─────────────────────────────────────────────────────────────
            // 4. Build the parameters map.
            //    Previously this was passed as  new HashMap<>()  (empty!),
            //    which meant every $P{...} field printed null/0.
            //    Now we populate all header-level values from the Order and
            //    the currently logged-in User.
            // ─────────────────────────────────────────────────────────────
            User user = MainApp.getCurrentUser();

            String address = "N/A";
            String phone   = "N/A";
            if (user != null) {
                if (user.getAddress()     != null && !user.getAddress().isEmpty())     address = user.getAddress();
                if (user.getGcashNumber() != null && !user.getGcashNumber().isEmpty()) phone   = user.getGcashNumber();
            }

            HashMap<String, Object> params = new HashMap<>();
            params.put("orderId",      order.getId());
            params.put("customerName", order.getCustomerName());
            params.put("orderDate",    order.getOrderDate()
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            params.put("total",        order.getTotal());
            params.put("address",      address);
            params.put("phone",        phone);

            // 5. Fill the report and export to PDF
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, xmlDataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

            // 6. Clean up the temporary XML file
            new File(xmlDataPath).delete();
            System.out.println("✅ PDF generated successfully: " + outputPath);

        } catch (Exception e) {
            System.err.println("❌ Failed to generate receipt:");
            e.printStackTrace();
        }
    }

    /**
     * Builds the XML data file that JRXmlDataSource will read.
     *
     * Structure:
     * <Order>
     *   <OrderId>…</OrderId>
     *   <CustomerName>…</CustomerName>
     *   <OrderDate>…</OrderDate>
     *   <Total>…</Total>
     *   <Address>…</Address>
     *   <Phone>…</Phone>
     *   <Items>
     *     <Item>
     *       <ProductName>…</ProductName>
     *       <Price>…</Price>
     *       <Quantity>…</Quantity>
     *     </Item>
     *     …
     *   </Items>
     * </Order>
     *
     * The JRXmlDataSource XPath "/Order/Items/Item" iterates over <Item> nodes.
     * In the JRXML, each field's <fieldDescription> maps to the child element
     * name inside <Item> (e.g. ProductName, Price, Quantity).
     */
    private static void generateXmlDataFile(Order order, String xmlFilePath) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();

        Element root = doc.createElement("Order");
        doc.appendChild(root);

        // Order-level metadata (accessible as $P{} parameters in the report)
        root.appendChild(elem(doc, "OrderId",      String.valueOf(order.getId())));
        root.appendChild(elem(doc, "CustomerName", order.getCustomerName()));
        root.appendChild(elem(doc, "OrderDate",
                order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
        root.appendChild(elem(doc, "Total",        String.format("%.2f", order.getTotal())));

        User user = MainApp.getCurrentUser();
        root.appendChild(elem(doc, "Address",
                (user != null && user.getAddress() != null && !user.getAddress().isEmpty())
                        ? user.getAddress() : "N/A"));
        root.appendChild(elem(doc, "Phone",
                (user != null && user.getGcashNumber() != null && !user.getGcashNumber().isEmpty())
                        ? user.getGcashNumber() : "N/A"));

        // Item rows (iterated by JRXmlDataSource via $F{} fields in the report)
        Element items = doc.createElement("Items");
        root.appendChild(items);

        for (OrderItem oi : order.getItems()) {
            Element item = doc.createElement("Item");
            // Element names here MUST match the <fieldDescription> values in receipt.jrxml
            item.appendChild(elem(doc, "ProductName", oi.getProductName()));
            item.appendChild(elem(doc, "Price",       String.valueOf(oi.getProductPrice())));
            item.appendChild(elem(doc, "Quantity",    String.valueOf(oi.getQuantity())));
            items.appendChild(item);
        }

        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(doc), new StreamResult(new File(xmlFilePath)));
    }

    private static Element elem(Document doc, String name, String value) {
        Element e = doc.createElement(name);
        e.appendChild(doc.createTextNode(value != null ? value : ""));
        return e;
    }
}
