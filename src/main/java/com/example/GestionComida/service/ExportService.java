package com.example.GestionComida.service;

import com.example.GestionComida.domain.entity.Order;
import com.example.GestionComida.domain.entity.Product;
import com.example.GestionComida.repo.OrderRepository;
import com.example.GestionComida.repo.ProductRepository;
import com.example.GestionComida.web.dto.report.SalesByDayDto;
import com.example.GestionComida.web.dto.report.SalesBySellerDto;
import com.example.GestionComida.web.dto.report.TopProductDto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {
    
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final ReportService reportService;

    public byte[] exportOrdersToExcel() throws IOException {
        List<Order> orders = orderRepo.findAll();
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Pedidos");
            
            // Header
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            String[] headers = {"ID", "Cliente", "Vendedor", "Estado", "Total", "Dirección", "Método Pago", "Fecha"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data
            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getClient() != null ? order.getClient().getName() : "");
                row.createCell(2).setCellValue(order.getSeller() != null ? order.getSeller().getName() : "Sin asignar");
                row.createCell(3).setCellValue(order.getStatus());
                row.createCell(4).setCellValue(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0);
                row.createCell(5).setCellValue(order.getDeliveryAddress());
                row.createCell(6).setCellValue(order.getPaymentMethod());
                row.createCell(7).setCellValue(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportProductsToExcel() throws IOException {
        List<Product> products = productRepo.findAll();
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Productos");
            
            // Header
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            String[] headers = {"ID", "Nombre", "Descripción", "Precio", "Stock", "Categoría", "Disponible"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data
            int rowNum = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getDescription());
                row.createCell(3).setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
                row.createCell(4).setCellValue(product.getStock());
                row.createCell(5).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "");
                row.createCell(6).setCellValue(product.getIsAvailable() ? "Sí" : "No");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportReportsToExcel(LocalDate from, LocalDate to) throws IOException {
        List<SalesByDayDto> byDay = reportService.ventasPorDia(from, to);
        List<SalesBySellerDto> bySeller = reportService.ventasPorVendedor(from, to);
        List<TopProductDto> topProducts = reportService.topProductos(from, to, 10);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Sheet 1: Ventas por día
            Sheet sheet1 = workbook.createSheet("Ventas por Día");
            createSalesByDaySheet(workbook, sheet1, byDay);
            
            // Sheet 2: Ventas por vendedor
            Sheet sheet2 = workbook.createSheet("Ventas por Vendedor");
            createSalesBySellerSheet(workbook, sheet2, bySeller);
            
            // Sheet 3: Top productos
            Sheet sheet3 = workbook.createSheet("Top Productos");
            createTopProductsSheet(workbook, sheet3, topProducts);
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createSalesByDaySheet(Workbook workbook, Sheet sheet, List<SalesByDayDto> data) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = getHeaderStyle(workbook);
        
        String[] headers = {"Fecha", "Cantidad Pedidos", "Total Ventas"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (SalesByDayDto dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getFecha() != null ? dto.getFecha().toString() : "");
            row.createCell(1).setCellValue(dto.getCantidadPedidos() != null ? dto.getCantidadPedidos() : 0);
            row.createCell(2).setCellValue(dto.getTotalVentas() != null ? dto.getTotalVentas().doubleValue() : 0.0);
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSalesBySellerSheet(Workbook workbook, Sheet sheet, List<SalesBySellerDto> data) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = getHeaderStyle(workbook);
        
        String[] headers = {"ID Vendedor", "Nombre Vendedor", "Cantidad Pedidos", "Total Ventas"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (SalesBySellerDto dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getVendedorId() != null ? dto.getVendedorId() : 0);
            row.createCell(1).setCellValue(dto.getVendedorNombre() != null ? dto.getVendedorNombre() : "");
            row.createCell(2).setCellValue(dto.getCantidadPedidos() != null ? dto.getCantidadPedidos() : 0);
            row.createCell(3).setCellValue(dto.getTotalVentas() != null ? dto.getTotalVentas().doubleValue() : 0.0);
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTopProductsSheet(Workbook workbook, Sheet sheet, List<TopProductDto> data) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = getHeaderStyle(workbook);
        
        String[] headers = {"ID Producto", "Nombre Producto", "Cantidad Vendida", "Total Ventas"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (TopProductDto dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getProductoId() != null ? dto.getProductoId() : 0);
            row.createCell(1).setCellValue(dto.getProductoNombre() != null ? dto.getProductoNombre() : "");
            row.createCell(2).setCellValue(dto.getCantidadVendida() != null ? dto.getCantidadVendida() : 0);
            row.createCell(3).setCellValue(dto.getTotalVentas() != null ? dto.getTotalVentas().doubleValue() : 0.0);
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        return headerStyle;
    }
}
