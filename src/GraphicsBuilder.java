import java.awt.*;
import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.*;
import org.jfree.chart.plot.PlotOrientation;

import java.util.ArrayList;

public class GraphicsBuilder {
    public void showHistogram(Collection<Double> scoresCollection, String title) {
        var scores = scoresCollection.stream().mapToDouble(d -> d).toArray();
        var dataset = new DefaultCategoryDataset();
        dataset.setType(HistogramType.FREQUENCY);
        dataset.addSeries("Histogram", scores, scores.length);
        var xaxis = "Значения";
        var yaxis = "Частота";
        var orientation = PlotOrientation.VERTICAL;
        var show = false;
        var toolTips = false;
        var urls = false;
        var chart = ChartFactory.createBarChart(title, xaxis, yaxis,
                dataset, orientation, show, toolTips, urls);

        var plot = (CategoryPlot) chart.getPlot();
        var renderer = new IntervalBarRenderer();
        renderer.setMaximumBarWidth(0.2);



        // Добавляем гистограмму в окно
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartPanel.setChart(chart);
    }
}
