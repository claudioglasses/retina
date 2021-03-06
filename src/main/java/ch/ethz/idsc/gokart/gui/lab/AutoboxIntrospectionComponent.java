// code by jph
package ch.ethz.idsc.gokart.gui.lab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ch.ethz.idsc.gokart.core.AutoboxSocket;
import ch.ethz.idsc.gokart.core.AutoboxSockets;
import ch.ethz.idsc.gokart.core.ProviderRanks;
import ch.ethz.idsc.owl.ani.api.ProviderRank;

public class AutoboxIntrospectionComponent {
  private static final int NUMEL = 4;
  private static final int LENGTH = 3;
  private final JTextField[] jTextField = new JTextField[NUMEL];
  private final JLabel[][] jLabel = new JLabel[NUMEL][LENGTH];
  public final JPanel jPanel = new JPanel(new BorderLayout());

  public AutoboxIntrospectionComponent() {
    { // title
      JPanel jPanelTitle = new JPanel(new GridLayout(NUMEL, 1));
      for (AutoboxSocket<?, ?> autoboxSocket : AutoboxSockets.ALL) {
        String title = autoboxSocket.getClass().getSimpleName();
        jPanelTitle.add(new JLabel(title.substring(0, title.length() - 6)));
      }
      jPanel.add(BorderLayout.WEST, jPanelTitle);
    }
    JPanel jPanelTextField = new JPanel(new GridLayout(NUMEL, 1));
    { // provider
      int index = 0;
      for (AutoboxSocket<?, ?> autoboxSocket : AutoboxSockets.ALL) {
        jTextField[index] = new JTextField();
        jTextField[index].setEditable(false);
        jTextField[index].setToolTipText(autoboxSocket.getClass().getSimpleName());
        jPanelTextField.add(jTextField[index]);
        ++index;
      }
      jPanel.add(BorderLayout.CENTER, jPanelTextField);
    }
    JPanel jPanelCounts = new JPanel(new GridLayout(NUMEL, LENGTH));
    { // size of providers and listeners
      for (int index = 0; index < NUMEL; ++index)
        for (int count = 0; count < jLabel[index].length; ++count) {
          jLabel[index][count] = new JLabel("?", SwingConstants.CENTER);
          jLabel[index][count].setPreferredSize(new Dimension(24, 18));
          jPanelCounts.add(jLabel[index][count]);
        }
      jPanel.add(BorderLayout.EAST, jPanelCounts);
    }
  }

  public void update() {
    int index = 0;
    for (AutoboxSocket<?, ?> autoboxSocket : AutoboxSockets.ALL) {
      {
        jTextField[index].setText(autoboxSocket.getPutProviderDesc());
        Optional<ProviderRank> optional = autoboxSocket.getPutProviderRank();
        jTextField[index].setBackground(optional.isPresent() //
            ? ProviderRanks.color(optional.get())
            : Color.WHITE);
      }
      jLabel[index][0].setText("" + autoboxSocket.getPutProviderSize());
      jLabel[index][1].setText("" + autoboxSocket.getPutListenersSize());
      jLabel[index][2].setText("" + autoboxSocket.getGetListenersSize());
      ++index;
    }
  }
}
