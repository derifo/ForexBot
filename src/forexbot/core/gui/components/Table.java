package forexbot.core.gui.components;

public interface Table {
	
	public String[] ColumnNames();
	public Object[][] TableData();
	public void UpdateTableData(Object[][] data);
	public int EditableRange();
	public void UpdateTable();
}
