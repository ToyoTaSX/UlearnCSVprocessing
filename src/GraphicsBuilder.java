import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;

public class GraphicsBuilder {
    public JFreeChart getPieChart(HashMap<String, Long> classesCounts, String title) {
        var dataset = new DefaultPieDataset();
        for (var entry : classesCounts.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return ChartFactory.createPieChart(
                title,  // заголовок диаграммы
                dataset,               // датасет
                true,                  // легенда
                true,
                false
        );
    }

    public JFreeChart getBarChart(HashMap<String, Long> data, String title, String xTitle, String yTitle)
    {
        Map<String, Integer> sortedData = data.entrySet()
                .stream()
                .sorted((e1, e2) -> -1 * e1.getValue().compareTo(e2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().intValue(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // Create dataset
        var dataset = new DefaultCategoryDataset();
        for (var entry : sortedData.entrySet()) {
            dataset.addValue(entry.getValue(), "Category", entry.getKey());
        }

        var chart = ChartFactory.createBarChart(
                title,   // chart title
                xTitle,                 // domain axis label
                yTitle,                  // range axis label
                dataset,                  // data
                PlotOrientation.HORIZONTAL, // orientation
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // Определение фона диаграммы
        chart.setBackgroundPaint(Color.white);
        return chart;
    }

    public void showChart(JFreeChart chart) {
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame(chart.getTitle().getText());
            //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ChartPanel chartPanel = new ChartPanel(chart);
            //chartPanel.setPreferredSize(new Dimension(560, 370));
            frame.setContentPane(chartPanel);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
