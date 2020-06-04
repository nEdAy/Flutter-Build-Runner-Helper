package cn.neday.excavator.tool;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ConsoleToolFactory implements ToolWindowFactory {
    private JPanel mPanel;
    private JScrollPane mScrollPane;
    private JTextArea mTxtContent;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mPanel, "Logcat", false);
        toolWindow.getContentManager().addContent(content);

        // 禁止编辑
        mTxtContent.setEditable(false);

        // 去除边框
        mTxtContent.setBorder(BorderFactory.createLineBorder(JBColor.BLACK, 0));
        mScrollPane.setBorder(BorderFactory.createLineBorder(JBColor.BLACK, 0));
        mPanel.setBorder(BorderFactory.createLineBorder(JBColor.BLACK, 0));

        // 设置透明
        mPanel.setOpaque(false);
        mScrollPane.setOpaque(false);
        mScrollPane.getViewport().setOpaque(false);
        mTxtContent.setOpaque(false);

        // 鼠标事件
        mTxtContent.removeMouseListener(mouseListener);
        mTxtContent.addMouseListener(mouseListener);

        // 鼠标事件
        mTxtContent.removeMouseListener(mouseAdapter);
        mTxtContent.addMouseListener(mouseAdapter);

        // 输入变化事件
        mTxtContent.getCaret().removeChangeListener(changeListener);
        mTxtContent.getCaret().addChangeListener(changeListener);
    }

    /**
     * 鼠标进出/入事件
     */
    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        public void mouseEntered(MouseEvent mouseEvent) {
            // 鼠标进入Text区后变为文本输入指针
            mTxtContent.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        }

        public void mouseExited(MouseEvent mouseEvent) {
            // 鼠标离开Text区后恢复默认形态
            mTxtContent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    };

    /**
     * 鼠标改变事件
     */
    private final ChangeListener changeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            // 使Text区的文本光标显示
            mTxtContent.getCaret().setVisible(true);
        }
    };

    /**
     * 鼠标右键事件
     */
    private final MouseListener mouseListener = new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == 3) { // 鼠标右键
                // 添加右键菜单的内容
                JBList<String> list = new JBList<>();
                String[] title = new String[2];
                title[0] = "    Select All";
                title[1] = "    Clear All";

                // 设置数据
                list.setListData(title);
                list.setFocusable(false);

                // 设置边框
                Border lineBorder = BorderFactory.createLineBorder(JBColor.BLACK, 1);
                list.setBorder(lineBorder);

                // 创建菜单 添加点击项的监听事件
                JBPopup popup = new PopupChooserBuilder(list)
                        .setItemChoosenCallback(() -> {
                            String value = list.getSelectedValue();
                            if (value.contains("Clear All")) {
                                mTxtContent.setText("");
                            } else if (value.contains("Select All")) {
                                mTxtContent.selectAll();
                            }
                        }).createPopup();

                // 设置大小
                Dimension dimension = popup.getContent().getPreferredSize();
                popup.setSize(new Dimension(150, dimension.height));

                // 传入e，获取位置进行显示
                popup.show(new RelativePoint(e));
                list.clearSelection();

                // 添加鼠标进入List事件
                list.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        super.mouseEntered(e);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        super.mouseExited(e);
                        list.clearSelection();
                    }
                });
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    };
}
