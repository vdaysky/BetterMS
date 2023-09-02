package obfuscate.util.chat;

import java.util.ArrayList;
import java.util.Collections;

public class ChatTable {

    private final ArrayList<ArrayList<String>> table = new ArrayList<>();

    public void addRow(ArrayList<String> row) {
        table.add(row);
    }

    public void addRow(String... row) {
        var rowList = new ArrayList<String>();
        Collections.addAll(rowList, row);
        addRow(rowList);
    }

    public ArrayList<ArrayList<String>> alignAndPrepare() {

        if (table.isEmpty()) {
            return new ArrayList<>();
        }

        var columnWidths = new Integer[table.get(0).size()];

        for (ArrayList<String> row : table) {
            for (int i = 0; i < row.size(); i++) {
                var cellWidth = row.get(i).length();
                columnWidths[i] = Math.max(
                    columnWidths[i] == null ? 0 : columnWidths[i],
                    cellWidth
                );
            }
        }

        for (var row : table) {
            for (int i = 0; i < row.size(); i++) {
                var columnWidth = columnWidths[i];
                var cellWidth = row.get(i).length();

                if (cellWidth < columnWidth) {
                    var padding = columnWidth - cellWidth;
                    var paddingString = " ".repeat(padding);
                    row.set(i, row.get(i) + paddingString);
                }
            }
        }
        return table;
    }
}
