package utils;

import dataLayer.Visit;
import dataLayer.WaitingHistoryItem;
import dataLayer.Member;
import dataLayer.Reservation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for generating PDF reports using PDFBox and JFreeChart.
 * capable of creating Time/Punctuality reports and Member Activity reports.
 */
public class ReportGenerator {

    private static final int CHART_WIDTH = 500;
    private static final int CHART_HEIGHT = 300;
    private static final int STANDARD_DURATION_MIN = 120; 
    
    /**
     * Generates a Time &amp; Punctuality Report for a list of visits for a specific month/year.
     * The report includes charts for punctuality and visit duration, and a detailed visit log.
     *
     * @param visits List of Visit objects to be included in the report
     * @param month the month for the report
     * @param year the year for the report
     */
    public static void generateTimeReport(List<Visit> visits, int month, int year) {
        String filename = "reports/Time_Report_" + month + "_" + year + ".pdf";
        
        File directory = new File("reports");//Create directory if not exists
        if (!directory.exists()) directory.mkdirs();
        
        try (PDDocument document = new PDDocument()) {
            PDPage page1 = new PDPage(PDRectangle.A4);//new page for pdf
            document.addPage(page1);
            PDPageContentStream contentStream = new PDPageContentStream(document, page1);
            //Report titles
            drawText(contentStream, "Time & Punctuality Report", 50, 750, 24, true);
            drawText(contentStream, "Period: " + month + "/" + year, 50, 720, 16, false);
            drawText(contentStream, "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), 50, 700, 12, false);
            //(Pie Chart) for punctuality
            JFreeChart punctualityChart = createPunctualityChart(visits);
            BufferedImage pChartImg = punctualityChart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
            drawChartOnPdf(document, contentStream, pChartImg, 50, 380);
            //(Bar Chart) for visit duration
            JFreeChart durationChart = createDurationChart(visits);
            BufferedImage dChartImg = durationChart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
            drawChartOnPdf(document, contentStream, dChartImg, 50, 50);
            contentStream.close(); //End page 1
            //Start page 2 - n
            PDPage page2 = new PDPage(PDRectangle.A4);
            document.addPage(page2);
            PDPageContentStream contentStream2 = new PDPageContentStream(document, page2);
            
            //Title
            drawText(contentStream2, "Visit Log", 50, 750, 18, true);
            int yPosition = 700;
            int margin = 50;
            
            // Table Header
            String header = String.format("%-12s %-15s %-10s %-10s %-10s %-10s","Date", "Guest", "Reserved", "Arrived", "Late(m)", "Duration");
            drawText(contentStream2, header, margin, yPosition, 10, true);
            drawLine(contentStream2, margin, yPosition - 5, 550, yPosition - 5);
            yPosition -= 20;

            for (Visit v : visits) {
                if (yPosition < 50) { // New page if full
                    contentStream2.close();
                    page2 = new PDPage(PDRectangle.A4);
                    document.addPage(page2);
                    contentStream2 = new PDPageContentStream(document, page2);
                    yPosition = 750;
                }
                
                String rowData = formatVisitRow(v);
                drawText(contentStream2, rowData, margin, yPosition, 10, false);
                yPosition -= 15;
            }
            
            contentStream2.close();
            document.save(filename);
            System.out.println("Time Report generated: " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generates a Member Activity Report.
     * Compares Members vs Guests, and Pre-booked vs Walk-in statistics.
     *
     * @param visits List of visit objects
     * @param waitingList List of waiting visit objects
     * @param month the month for the report
     * @param year the year for the report
     */
    public static void generateMemberReport(List<Visit> visits, List<WaitingHistoryItem> waitingList, int month, int year) {
        String filename = "reports/Member_Report_" + month + "_" + year + ".pdf";
        File directory = new File("reports");
        if (!directory.exists()) directory.mkdirs(); //Create reports dir if not exists

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            //Titles
            drawText(contentStream, "Members & Activity Report", 50, 750, 24, true);
            drawText(contentStream, "Period: " + month + "/" + year, 50, 720, 16, false);

            DefaultPieDataset pieDataset = new DefaultPieDataset();
            long memberVisits = visits.stream().filter(v -> v.getGuest() instanceof Member || v.getReservation() != null && v.getReservation().getMemberId() != null).count();
            long guestVisits = visits.size() - memberVisits;
            
            pieDataset.setValue("Members (" + memberVisits + ")", memberVisits);
            pieDataset.setValue("Guests (" + guestVisits + ")", guestVisits);
            //Pie chart
            JFreeChart pieChart = ChartFactory.createPieChart("Visits Distribution", pieDataset, true, true, false);
            BufferedImage pieImg = pieChart.createBufferedImage(500, 250);
            drawChartOnPdf(document, contentStream, pieImg, 50, 450);

            DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
            long membersFromWaiting = waitingList.size(); 
            long membersPreBooked = visits.stream()
                    .filter(v -> v.getReservation() != null && (v.getGuest() instanceof Member || v.getReservation().getMemberId() != null))
                    .count();

            barDataset.addValue(membersPreBooked, "Count", "Reserved");
            barDataset.addValue(membersFromWaiting, "Count", "Joined Waiting List");

            JFreeChart barChart = ChartFactory.createBarChart("Member Demand Source", "Method", "Count", 
                    barDataset, PlotOrientation.VERTICAL, false, true, false);
            BufferedImage barImg = barChart.createBufferedImage(500, 250);
            drawChartOnPdf(document, contentStream, barImg, 50, 150);

            contentStream.close();
            document.save(filename);
            System.out.println("Member Report generated: " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a Pie Chart visualization for visit punctuality (On Time vs Late vs Walk-in).
     *
     * @param visits List of Visit objects
     * @return JFreeChart PieChart object
     */
    private static JFreeChart createPunctualityChart(List<Visit> visits) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        int onTime = 0;
        int late = 0;
        int walkIn = 0; //walkin on time
        for (Visit v : visits) {
            if (v.getReservation() == null) {
                walkIn++;
                continue;
            }
            long lateMinutes = calculateLateMinutes(v);
            if (lateMinutes <= 0) {
                onTime++;
            } else {
                late++;
            }
        }
        dataset.setValue("On Time", onTime);
        dataset.setValue("Late", late);
        dataset.setValue("Walk-in", walkIn);
        return ChartFactory.createPieChart("Punctuality Breakdown", dataset, true, true, false);
    }
    
    /**
     * Creates a Bar Chart visualization for visit duration categorization.
     *
     * @param visits List of Visit objects
     * @return JFreeChart BarChart object
     */
    private static JFreeChart createDurationChart(List<Visit> visits) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int shortStay = 0;// < 1 hour
        int normalStay = 0;// 1 - 2 hours
        int overStay = 0;// > 2 hours

        for (Visit v : visits) {
            long duration = calculateDurationMinutes(v);
            if (duration < 60) shortStay++;
            else if (duration <= STANDARD_DURATION_MIN) normalStay++;
            else overStay++;
        }

        dataset.addValue(shortStay, "Visits", "< 1 Hour");
        dataset.addValue(normalStay, "Visits", "1 - 2 Hours");
        dataset.addValue(overStay, "Visits", "Overstay (>2h)");

        return ChartFactory.createBarChart("Dining Duration", "Time Category", "Number of Visits", 
                dataset, PlotOrientation.VERTICAL, false, true, false);
    }

    /**
     * Calculates the difference in minutes between the reservation time and the arrival time.
     *
     * @param v the visit object
     * @return long representing minutes late (positive) or early (negative/zero)
     */
    private static long calculateLateMinutes(Visit v) {
        try {
            Reservation res = v.getReservation();
            if (res == null) return 0;
            
            LocalDateTime resTime = parseDateTime(res.getReservationDate().getDate(), res.getReservationDate().getTime());
            LocalDateTime arrivalTime = parseDateTime(v.getStartTime().getDate(), v.getStartTime().getTime());
            
            long diff = Duration.between(resTime, arrivalTime).toMinutes();
            return diff;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calculates the total duration of a visit in minutes.
     *
     * @param v Visit object to calculate
     * @return long representing total minutes, or 0 on failure
     */
    private static long calculateDurationMinutes(Visit v) {
        try {
            LocalDateTime start = parseDateTime(v.getStartTime().getDate(), v.getStartTime().getTime());
            LocalDateTime end = parseDateTime(v.getEndTime().getDate(), v.getEndTime().getTime());
            return Duration.between(start, end).toMinutes();//returns long
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Parses date and time strings into a LocalDateTime object.
     * Handles formatting and string cleanup.
     *
     * @param date String represents date: yyyy-MM-dd
     * @param time String represents time: HH:mm:ss
     * @return parsed LocalDateTime object
     */
    private static LocalDateTime parseDateTime(String date, String time) {
        date = date.trim();
        time = time.trim();

        if (time.length() == 5) {
            time += ":00";
        }
        if (time.length() > 8) {
            time = time.substring(0, 8);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(date + " " + time, formatter);
    }
    
    /**
     * Helper method to write text into the PDF stream at a specific location.
     *
     * @param stream the PDF content stream
     * @param text the string to write
     * @param x x-coordinate
     * @param y y-coordinate
     * @param fontSize font size
     * @param bold true for bold font, false for standard
     * @throws IOException if writing to stream fails
     */
    private static void drawText(PDPageContentStream stream, String text, float x, float y, int fontSize, boolean bold) throws IOException {
        stream.beginText();
        stream.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }
    
    /**
     * Helper method to draw a line in the PDF.
     *
     * @param stream the PDF content stream
     * @param x1 start x-coordinate
     * @param y1 start y-coordinate
     * @param x2 end x-coordinate
     * @param y2 end y-coordinate
     * @throws IOException if drawing fails
     */
    private static void drawLine(PDPageContentStream stream, float x1, float y1, float x2, float y2) throws IOException {
        stream.moveTo(x1, y1);
        stream.lineTo(x2, y2);
        stream.stroke();
    }
    
    /**
     * Helper method to insert a buffered image (chart) into the PDF.
     *
     * @param doc the PDF document
     * @param stream the PDF content stream
     * @param image  the BufferedImage to draw
     * @param x x-coordinate
     * @param y y-coordinate
     * @throws IOException if image creation or drawing fails
     */
    private static void drawChartOnPdf(PDDocument doc, PDPageContentStream stream, BufferedImage image, float x, float y) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, baos.toByteArray(), "chart");
        stream.drawImage(pdImage, x, y);
    }
    
    /**
     * Formats a Visit object into a string row for the PDF table log.
     *
     * @param v Visit object to write
     * @return formatted String row
     */
    private static String formatVisitRow(Visit v) {
        String name = (v.getGuest() != null) ? v.getGuest().getFullName() : "Unknown";
        if (name.length() > 15) name = name.substring(0, 12) + "...";
        
        String resTime = (v.getReservation() != null) ? v.getReservation().getReservationDate().getTime() : "Walk-in";
        String arrTime = v.getStartTime().getTime();
        String late = (v.getReservation() != null) ? String.valueOf(calculateLateMinutes(v)) : "-";
        String duration = calculateDurationMinutes(v) + "m";
        
        return String.format("%-12s %-15s %-10s %-10s %-10s %-10s", 
                v.getStartTime().getDate(), name, resTime, arrTime, late, duration);
    }
}