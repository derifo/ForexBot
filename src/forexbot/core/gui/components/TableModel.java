package forexbot.core.gui.components;

import javax.swing.table.AbstractTableModel;

public class TableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TableModel(Table frame){
		this.frame = frame;
		
	}
	
    private static final boolean DEBUG = false;
    
	Table frame;

    public int getColumnCount() {
        return frame.ColumnNames().length;
    }
    public int getRowCount() {
        return frame.TableData().length;
    }

    public String getColumnName(int col) {
        return frame.ColumnNames()[col];
    }

    public Object getValueAt(int row, int col) {
        return frame.TableData()[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < frame.EditableRange()) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        if (DEBUG) {
            System.out.println("Setting value at " + row + "," + col
                               + " to " + value
                               + " (an instance of "
                               + value.getClass() + ")");
        }

        Object[][] temp = frame.TableData();
        temp[row][col] = value;
        frame.UpdateTableData(temp);
        fireTableCellUpdated(row, col);

        if (DEBUG) {
            System.out.println("New value of data:");
            printDebugData();
        }
    }

    private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + frame.TableData()[i][j]);
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
}
