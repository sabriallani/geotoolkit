/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2007 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2009, Johann Sorel
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
package org.geotoolkit.gui.swing.style;

import javax.swing.JPanel;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.StyleConstants;

import org.jdesktop.swingx.JXTitledPanel;
import org.opengis.style.TextSymbolizer;

/**
 * Text Symbolizer edition panel
 * 
 * @author Johann Sorel
 */
public class JTextSymbolizerPane extends StyleElementEditor<TextSymbolizer> {
    
    private MapLayer layer = null;

    /** Creates new form JTextSymbolizer */
    public JTextSymbolizerPane() {
        initComponents();
            }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setLayer(MapLayer layer) {
        this.layer = layer;
        guiFill.setLayer(layer);
        guiHalo.setLayer(layer);
        guiFont.setLayer(layer);
        guiGeom.setLayer(layer);
        guiLabel.setLayer(layer);
        guiPlacement.setLayer(layer);
            }

    /**
     * {@inheritDoc }
     */
    @Override
    public MapLayer getLayer() {
        return layer;
            }

    /**
     * {@inheritDoc }
     */
    @Override
    public TextSymbolizer create() {
        return getStyleFactory().textSymbolizer(
                "TextSymbolizer",
                guiGeom.getGeom(),
                StyleConstants.DEFAULT_DESCRIPTION,
                guiUOM.create(),
                guiLabel.create(), 
                guiFont.create(), 
                guiPlacement.create(), 
                guiHalo.create(), 
                guiFill.create() );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void parse(TextSymbolizer symbol) {
        if (symbol != null) {
            guiFill.parse(symbol.getFill());
            guiLabel.parse(symbol.getLabel());
            guiGeom.setGeom(symbol.getGeometryPropertyName());
            guiFont.parse(symbol.getFont());
            guiHalo.parse(symbol.getHalo());
            guiPlacement.parse(symbol.getLabelPlacement());        
    }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new JPanel();
        guiGeom = new JGeomPane();
        guiUOM = new JUOMPane();
        guiLabel = new JTextExpressionPane();
        jLabel1 = new JLabel();
        guiFont = new JFontPane();
        guiHalo = new JHaloPane();
        guiFill = new JFillPane();
        guiPlacement = new JLabelPlacementPane();

        setOpaque(false);

        jPanel1.setBorder(BorderFactory.createTitledBorder(MessageBundle.getString("general"))); // NOI18N
        jPanel1.setOpaque(false);


        jLabel1.setText(MessageBundle.getString("label")); // NOI18N
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);




        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(Alignment.TRAILING, false)
                        .addComponent(guiUOM, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(guiGeom, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(guiLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(206, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(guiGeom, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiUOM, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(guiLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(SwingConstants.VERTICAL, new Component[] {guiLabel, jLabel1});

        guiFont.setBorder(BorderFactory.createTitledBorder(MessageBundle.getString("fonts"))); // NOI18N
        guiHalo.setBorder(BorderFactory.createTitledBorder(MessageBundle.getString("halo"))); // NOI18N
        guiFill.setBorder(BorderFactory.createTitledBorder(MessageBundle.getString("fill"))); // NOI18N
        guiPlacement.setBorder(BorderFactory.createTitledBorder(MessageBundle.getString("placement"))); // NOI18N
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(guiFont, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(guiFill, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(guiHalo, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(guiPlacement, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiFont, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiFill, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiHalo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiPlacement, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JFillPane guiFill;
    private JFontPane guiFont;
    private JGeomPane guiGeom;
    private JHaloPane guiHalo;
    private JTextExpressionPane guiLabel;
    private JLabelPlacementPane guiPlacement;
    private JUOMPane guiUOM;
    private JLabel jLabel1;
    private JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
