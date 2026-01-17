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

public class ReportGenerator {

    private static final int CHART_WIDTH = 500;
    private static final int CHART_HEIGHT = 300;
    private static final int STANDARD_DURATION_MIN = 120; 
    
    /**
     * Generates timereport for List of Visit objects on month/year
     * @param visits List of Visit objects including information for report
     * @param month int for month number
     * @param year int for year number
     */
    public static void generateTimeReport(List<Visit> visits, int month, int year) {
        String filename = "reports/Time_Report_" + month + "_" + year + ".pdf";
        
        File directory = new File("reports");//Create direcotry if not exists
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
     * Generate report of members: Members vs. Guests, Reserved vs Walked-In
     * @param visits List of visit objects
     * @param waitingList List of waiting visit objects
     * @param month int for month number
     * @param year int for year number
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
     * Creates shart using List of Visit objects
     * @param visits List of Visit objects
     * @return PieChart for punctuality
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
     * Creates duration of visit chart 
     * @param visits List of Visit objects
     * @return BarChart for visit duration
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
     * Calculating visit v late time
     * @param wanted visit object in db
     * @return long thar represent minutes of difference between reservation and arrivals times.
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
     * Calculating visit duration time
     * @param v Visit object to calculate
     * @return return long, on failure 0
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
     * Parsing date and time for LocalDateTime
     * @param date String represents date: yyyy-MM-dd
     * @param time String represents time: HH:mm:ss
     * @return LocalDateTime
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
     * Private method to type text into pdf using PDFBOX
     * @param stream where to write (document)
     * @param text what to write (text)
     * @param x at what X of page
     * @param y at what Y of pgae
     * @param fontSize text font size
     * @param bold is it bold
     * @throws IOException
     */
    private static void drawText(PDPageContentStream stream, String text, float x, float y, int fontSize, boolean bold) throws IOException {
        stream.beginText();
        stream.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }
    
    /**
     * Private method to draw separating line in pdf
     * @param stream where to write
     * @param x1 where to start in x
     * @param y1 where to start in y
     * @param x2 where to end in x
     * @param y2 where to end in y
     * @throws IOException
     */
    private static void drawLine(PDPageContentStream stream, float x1, float y1, float x2, float y2) throws IOException {
        stream.moveTo(x1, y1);
        stream.lineTo(x2, y2);
        stream.stroke();
    }
    
    /**
     * Private method to insert chart created as bufferedimage
     * @param doc document to write to
     * @param stream stream of inserted data
     * @param image chart 
     * @param x where to put in x
     * @param y where to put in y
     * @throws IOException
     */
    private static void drawChartOnPdf(PDDocument doc, PDPageContentStream stream, BufferedImage image, float x, float y) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, baos.toByteArray(), "chart");
        stream.drawImage(pdImage, x, y);
    }
    
    /**
     * Writes visit log into visit logs table
     * @param v Visit object to write
     * @return String row to write to document
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