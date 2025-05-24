package com.chefsync;

import java.util.List;

/**
 * Utility class for formatting data as text tables
 */
public class TableFormatter {
    
    /**
     * Format a list of data rows as a table
     * 
     * @param headers Column headers
     * @param data List of Object arrays with row data
     * @param maxWidths Maximum width for each column
     * @return Formatted table string
     */
    public static String formatTable(String[] headers, List<Object[]> data, int[] maxWidths) {
        if (headers.length != maxWidths.length || (data.size() > 0 && data.get(0).length != headers.length)) {
            throw new IllegalArgumentException("Headers, data, and maxWidths must have the same length");
        }
        
        StringBuilder result = new StringBuilder();
        
        // Calculate actual column widths (minimum of maxWidth and the longest content)
        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = Math.min(maxWidths[i], Math.max(headers[i].length(), getMaxContentLength(data, i)));
        }
        
        // Create top border
        appendBorder(result, columnWidths);
        
        // Create header row
        result.append("| ");
        for (int i = 0; i < headers.length; i++) {
            result.append(padRight(headers[i], columnWidths[i])).append(" | ");
        }
        result.append("\n");
        
        // Create header/data separator
        appendBorder(result, columnWidths);
        
        // Create data rows
        for (Object[] row : data) {
            result.append("| ");
            for (int i = 0; i < row.length; i++) {
                String cellValue = row[i] != null ? row[i].toString() : "";
                result.append(padRight(limitString(cellValue, columnWidths[i]), columnWidths[i])).append(" | ");
            }
            result.append("\n");
        }
        
        // Create bottom border
        appendBorder(result, columnWidths);
        
        return result.toString();
    }
    
    private static void appendBorder(StringBuilder sb, int[] columnWidths) {
        sb.append("+");
        for (int width : columnWidths) {
            sb.append("-".repeat(width + 2)).append("+");
        }
        sb.append("\n");
    }
    
    private static int getMaxContentLength(List<Object[]> data, int columnIndex) {
        int maxLength = 0;
        for (Object[] row : data) {
            if (columnIndex < row.length && row[columnIndex] != null) {
                maxLength = Math.max(maxLength, row[columnIndex].toString().length());
            }
        }
        return maxLength;
    }
    
    private static String padRight(String s, int n) {
        if (s.length() >= n) {
            return s;
        }
        return s + " ".repeat(n - s.length());
    }
    
    private static String limitString(String input, int maxLength) {
        if (input == null) return "";
        return input.length() <= maxLength ? input : input.substring(0, maxLength - 3) + "...";
    }
} 