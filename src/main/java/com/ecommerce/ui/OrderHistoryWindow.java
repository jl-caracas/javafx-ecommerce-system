package com.ecommerce.ui;

import com.ecommerce.db.OrderDAO;
import com.ecommerce.model.Order;
import com.ecommerce.report.ReceiptGenerator;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderHistoryWindow {

    private Stage stage;

    public OrderHistoryWindow() {
        stage = new Stage();
        stage.setTitle("CØDE LØCK - Order History");
        
        stage.setMinWidth(650);
        stage.setMinHeight(400);
        
        buildUI();
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        TableView<Order> table = new TableView<>();
        
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
     
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMinWidth(60); 
        idCol.setPrefWidth(60);

        // Name Column
        TableColumn<Order, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        nameCol.setMinWidth(160);
        nameCol.setPrefWidth(200);

        // Date Colum
        TableColumn<Order, LocalDateTime> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        dateCol.setMinWidth(160);
        dateCol.setPrefWidth(180);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        dateCol.setCellFactory(column -> {
            return new TableCell<Order, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });

        // Total Column: Slimmed down slightly
        TableColumn<Order, Double> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setMinWidth(90);
        totalCol.setPrefWidth(100);
        
        totalCol.setCellFactory(column -> {
            return new TableCell<Order, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(String.format("₱%.2f", item));
                        setStyle("-fx-font-weight: bold;"); 
                    }
                }
            };
        });

        TableColumn<Order, Void> actionCol = new TableColumn<>("Receipt");
        actionCol.setMinWidth(90); 
        actionCol.setPrefWidth(90);
        actionCol.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button btn = new Button("📄 View");

            {
                btn.getStyleClass().add("btn-secondary");
                
                btn.setOnAction(event -> {
                    Order summaryOrder = getTableView().getItems().get(getIndex());
                    OrderDAO orderDAO = new OrderDAO();
                    
                    Order fullOrder = orderDAO.getOrderById(summaryOrder.getId());
                    
                    if (fullOrder != null) {
                        String receiptPath = "receipt_history_" + fullOrder.getId() + ".pdf";
                        ReceiptGenerator.generateReceipt(fullOrder, receiptPath);
                        
                        try {
                            File pdfFile = new File(receiptPath);
                            if (pdfFile.exists() && java.awt.Desktop.isDesktopSupported()) {
                                java.awt.Desktop.getDesktop().open(pdfFile);
                            }
                        } catch (Exception ex) {
                            System.out.println("Could not open PDF automatically.");
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, dateCol, totalCol, actionCol);

        OrderDAO orderDAO = new OrderDAO();
        // Show only the current user's orders (or all orders if no user is logged in)
        com.ecommerce.model.User currentUser = MainApp.getCurrentUser();
        if (currentUser != null) {
            table.setItems(FXCollections.observableArrayList(orderDAO.getOrdersByUserId(currentUser.getId())));
        } else {
            table.setItems(FXCollections.observableArrayList(orderDAO.getAllOrders()));
        }

        VBox root = new VBox(10, table);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f7f6;"); 
        
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(root, 720, 450); 
        
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {}
        
        stage.setScene(scene);
    }

    public void show() {
        stage.centerOnScreen();
        stage.show();
    }
}