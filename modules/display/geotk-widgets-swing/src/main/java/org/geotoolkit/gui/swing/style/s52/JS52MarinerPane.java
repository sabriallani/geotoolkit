/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2013, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.gui.swing.style.s52;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import org.geotoolkit.gui.swing.misc.JOptionDialog;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.s52.S52Context;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.openide.util.NbBundle;

/**
 * S52 Mariner information editor.
 *
 * @author Johann Sorel (Geomatys)
 */
public class JS52MarinerPane extends javax.swing.JPanel {

    private final S52Context context;

    public JS52MarinerPane(final S52Context context) {
        initComponents();
        this.context = context;
        update();
    }

    /**
     * Call whenever S52Context changed.
     */
    public void update(){
        guiPalette.setModel(new ListComboBoxModel(context.getAvailablePalettes()));
        guiPalette.setSelectedItem(context.getActivePaletteName());
        guiPointTable.setModel(new ListComboBoxModel(context.getAvailablePointTables()));
        guiPointTable.setSelectedItem(context.getActivePointTable());
        guiLineTable.setModel(new ListComboBoxModel(context.getAvailableLineTables()));
        guiLineTable.setSelectedItem(context.getActiveLineTable());
        guiAreaTable.setModel(new ListComboBoxModel(context.getAvailableAreaTables()));
        guiAreaTable.setSelectedItem(context.getActiveAreaTable());

        guiSafetyDeph.setValue(context.getSafetyDepth());
        guiSafetyContour.setValue(context.getSafetyContour());
        guiShallowContour.setValue(context.getShallowContour());
        guiDeepContour.setValue(context.getDeepContour());
        guiDistanceTag.setValue(context.getDistanceTags());
        guiTimeTag.setValue(context.getTimeTags());

        guiShallowPatern.setSelected(context.isShallowPattern());
        guiTwoShades.setSelected(context.isTwoShades());
        guiNoText.setSelected(context.isNoText());
        guiLowAccuracySymbols.setSelected(context.isLowAccuracySymbols());
        guiShipOutline.setSelected(context.isShipsOutline());
        guiContourLabels.setSelected(context.isContourLabels());
        guiFullSectors.setSelected(context.isFullSectors());
        guiLightDescription.setSelected(context.isLightDescription());

    }

    /**
     * Apply all values on S52Context.
     */
    public void apply(){
        context.setActivePaletteName((String)guiPalette.getSelectedItem());
        context.setActivePointTable((String)guiPointTable.getSelectedItem());
        context.setActiveLineTable( (String)guiLineTable.getSelectedItem());
        context.setActiveAreaTable( (String)guiAreaTable.getSelectedItem());

        context.setSafetyDepth(     (Float)guiSafetyDeph.getValue());
        context.setSafetyContour(   (Float)guiSafetyContour.getValue());
        context.setShallowContour(  (Float)guiShallowContour.getValue());
        context.setDeepContour(     (Float)guiDeepContour.getValue());
        context.setDistanceTags(    (Float)guiDistanceTag.getValue());
        context.setTimeTags(        (Float)guiTimeTag.getValue());

        context.setShallowPattern(guiShallowPatern.isSelected());
        context.setTwoShades(guiTwoShades.isSelected());
        context.setNoText(guiNoText.isSelected());
        context.setLowAccuracySymbols(guiLowAccuracySymbols.isSelected());
        context.setShipsOutline(guiShipOutline.isSelected());
        context.setContourLabels(guiContourLabels.isSelected());
        context.setFullSectors(guiFullSectors.isSelected());
        context.setLightDescription(guiLightDescription.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        JPanel jPanel1 = new JPanel();
        JLabel jLabel4 = new JLabel();
        guiAreaTable = new JComboBox();
        guiLineTable = new JComboBox();
        JLabel jLabel3 = new JLabel();
        JLabel jLabel2 = new JLabel();
        guiPointTable = new JComboBox();
        guiPalette = new JComboBox();
        JLabel jLabel1 = new JLabel();
        JPanel jPanel2 = new JPanel();
        guiShallowPatern = new JCheckBox();
        guiTwoShades = new JCheckBox();
        guiNoText = new JCheckBox();
        guiLowAccuracySymbols = new JCheckBox();
        guiShipOutline = new JCheckBox();
        guiContourLabels = new JCheckBox();
        guiFullSectors = new JCheckBox();
        guiLightDescription = new JCheckBox();
        JPanel jPanel3 = new JPanel();
        JLabel jLabel5 = new JLabel();
        JLabel jlabel6 = new JLabel();
        JLabel jlabel7 = new JLabel();
        JLabel jlabel8 = new JLabel();
        JLabel jlabel9 = new JLabel();
        JLabel jlabel10 = new JLabel();
        guiSafetyDeph = new JSpinner();
        guiShallowContour = new JSpinner();
        guiSafetyContour = new JSpinner();
        guiDeepContour = new JSpinner();
        guiDistanceTag = new JSpinner();
        guiTimeTag = new JSpinner();
        guiDetails = new JButton();

        jPanel1.setBorder(BorderFactory.createEtchedBorder());

        jLabel4.setText(MessageBundle.getString("s52.areatable")); // NOI18N

        jLabel3.setText(MessageBundle.getString("s52.linetable")); // NOI18N

        jLabel2.setText(MessageBundle.getString("s52.pointtable")); // NOI18N

        jLabel1.setText(MessageBundle.getString("s52.palette")); // NOI18N

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiPalette, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiPointTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiLineTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiAreaTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {jLabel1, jLabel2, jLabel3, jLabel4});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(guiPalette, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(guiPointTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(guiLineTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(guiAreaTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(BorderFactory.createEtchedBorder());

        guiShallowPatern.setText(MessageBundle.getString("s52.shallowpattern")); // NOI18N

        guiTwoShades.setText(MessageBundle.getString("s52.twoshades")); // NOI18N

        guiNoText.setText(MessageBundle.getString("s52.notext")); // NOI18N

        guiLowAccuracySymbols.setText(MessageBundle.getString("s52.lowaccsymbol")); // NOI18N

        guiShipOutline.setText(MessageBundle.getString("s52.shipoutline")); // NOI18N

        guiContourLabels.setText(MessageBundle.getString("s52.contourlabel")); // NOI18N

        guiFullSectors.setText(MessageBundle.getString("s52.fullsector")); // NOI18N

        guiLightDescription.setText(MessageBundle.getString("s52.lightdescription")); // NOI18N

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(guiShallowPatern)
                    .addComponent(guiTwoShades)
                    .addComponent(guiNoText)
                    .addComponent(guiLowAccuracySymbols)
                    .addComponent(guiShipOutline)
                    .addComponent(guiContourLabels)
                    .addComponent(guiFullSectors)
                    .addComponent(guiLightDescription))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {guiContourLabels, guiFullSectors, guiLightDescription, guiLowAccuracySymbols, guiNoText, guiShallowPatern, guiShipOutline, guiTwoShades});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(guiShallowPatern)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiTwoShades)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiNoText)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiLowAccuracySymbols)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiShipOutline)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiContourLabels)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiFullSectors)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiLightDescription)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(BorderFactory.createEtchedBorder());

        jLabel5.setText(MessageBundle.getString("s52.safetydepth")); // NOI18N

        jlabel6.setText(MessageBundle.getString("s52.shallowcontour")); // NOI18N

        jlabel7.setText(MessageBundle.getString("s52.safetycontour")); // NOI18N

        jlabel8.setText(MessageBundle.getString("s52.deepcontour")); // NOI18N

        jlabel9.setText(MessageBundle.getString("s52.distancetag")); // NOI18N

        jlabel10.setText(MessageBundle.getString("s52.timetag")); // NOI18N

        guiSafetyDeph.setModel(new SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));

        guiShallowContour.setModel(new SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));

        guiSafetyContour.setModel(new SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));

        guiDeepContour.setModel(new SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));

        guiDistanceTag.setModel(new SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));

        guiTimeTag.setModel(new SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(1.0f)));

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiSafetyDeph, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jlabel10)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiTimeTag, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jlabel9)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiDistanceTag, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jlabel8)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiDeepContour, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jlabel7)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiSafetyContour, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jlabel6)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiShallowContour, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {jLabel5, jlabel10, jlabel6, jlabel7, jlabel8, jlabel9});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(guiSafetyDeph, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jlabel6)
                    .addComponent(guiShallowContour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jlabel7)
                    .addComponent(guiSafetyContour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jlabel8)
                    .addComponent(guiDeepContour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jlabel9)
                    .addComponent(guiDistanceTag, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jlabel10)
                    .addComponent(guiTimeTag, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        guiDetails.setText(MessageBundle.getString("s52.detail")); // NOI18N
        guiDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiDetailsActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiDetails))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(guiDetails))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void guiDetailsActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiDetailsActionPerformed

        final JS52PalettePane palettePane = new JS52PalettePane(context);
        final JS52SymbolPane stylePane = new JS52SymbolPane(context);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.add("Palettes", palettePane);
        tabs.add("Symbol styles", stylePane);

        for(String name : context.getAvailablePointTables()){
            tabs.add(name,new JS52LookupTablePane(context, context.getLookupTable(name)));
        }
        for(String name : context.getAvailableLineTables()){
            tabs.add(name,new JS52LookupTablePane(context, context.getLookupTable(name)));
        }
        for(String name : context.getAvailableAreaTables()){
            tabs.add(name,new JS52LookupTablePane(context, context.getLookupTable(name)));
        }

        JOptionDialog.show(this, tabs, JOptionPane.OK_OPTION);


    }//GEN-LAST:event_guiDetailsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox guiAreaTable;
    private JCheckBox guiContourLabels;
    private JSpinner guiDeepContour;
    private JButton guiDetails;
    private JSpinner guiDistanceTag;
    private JCheckBox guiFullSectors;
    private JCheckBox guiLightDescription;
    private JComboBox guiLineTable;
    private JCheckBox guiLowAccuracySymbols;
    private JCheckBox guiNoText;
    private JComboBox guiPalette;
    private JComboBox guiPointTable;
    private JSpinner guiSafetyContour;
    private JSpinner guiSafetyDeph;
    private JSpinner guiShallowContour;
    private JCheckBox guiShallowPatern;
    private JCheckBox guiShipOutline;
    private JSpinner guiTimeTag;
    private JCheckBox guiTwoShades;
    // End of variables declaration//GEN-END:variables
}
