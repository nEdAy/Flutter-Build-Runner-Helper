package cn.neday.excavator.setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class Setting implements Configurable {
    private JPanel mMainPanel;
    private TextFieldWithBrowseButton button;

    private String _lastValue;

    public static final String FLUTTER_PATH_KEY = "flutter_path";

    /**
     * 在settings中显示的名称
     *
     * @return 名称
     */
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Flutter Build Runner Helper";
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    /**
     * 初始化控件
     *
     * @return mMainPanel
     */
    @Override
    public @Nullable JComponent createComponent() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String flutterPath = propertiesComponent.getValue(FLUTTER_PATH_KEY);
        button.setText(flutterPath);
        _lastValue = flutterPath;
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        String flutterName = isWindows ? "flutter.bat" : "flutter";
        button.addBrowseFolderListener("Choose Your Flutter",
                "Please choose your flutter, maybe it path is 'flutterDir/bin/" + flutterName + "'.", null,
                FileChooserDescriptorFactory.createSingleFileDescriptor().withFileFilter(virtualFile -> {
                    String name = virtualFile.getName().toLowerCase();
                    return name.equals(flutterName);
                }));
        return mMainPanel;
    }

    @Override
    public boolean isModified() {
        return !_lastValue.equals(button.getText());
    }


    /**
     * 点击【apply】、【OK】时，调用
     */
    @Override
    public void apply() {
        String newValue = button.getText();
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(FLUTTER_PATH_KEY, newValue);
    }

    /**
     * 点击【Reset】时，调用
     */
    @Override
    public void reset() {
        // 重置数据
        button.setText(_lastValue);
    }
}
