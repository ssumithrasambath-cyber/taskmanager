
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class JProgressBarRenderer extends JProgressBar
        implements TableCellRenderer {

    public JProgressBarRenderer() {
        super(0, 100);
        setStringPainted(true);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {

        String progress = value.toString();
        int percentage = Integer.parseInt(
            progress.replace("%", "")
        );

        setValue(percentage);
        setString(percentage + "%");

        return this;
    }
}
