package com.torresj.footballteammanagementapi.services.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.torresj.footballteammanagementapi.dtos.MovementDto;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final MemberService memberService;
    private final MovementService movementService;
    private final TeamMovementService teamMovementService;

    private static final Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 26, Font.BOLDITALIC);
    private static final Font paragraphFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD);
    private static final Font headerMemberFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter formatterToSpanish = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] getBalancePDF() throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        //PDF title
        document.addTitle("Balance " + LocalDate.now().getYear());

        //Document title
        Paragraph title = new Paragraph(
                "Balance de la peña Km/h para la temporada " + LocalDate.now().getYear() + "\n\n", chapterFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Team balance section
        addTeamBalanceTable(document);

        // Members balance section
        addMembersBalanceTable(document);

        // Add final total balance
        addTotalTeamBalanceTable(document);

        document.close();
        return outputStream.toByteArray();
    }

    private void addTotalTeamBalanceTable(Document document) throws DocumentException{
        Paragraph totalBalance =
                new Paragraph("\n\nBalance total de la peña a fecha " + LocalDate.now().format(formatterToSpanish) + "\n\n", paragraphFont);
        totalBalance.setAlignment(Element.ALIGN_CENTER);

        document.add(totalBalance);

        PdfPTable table = new PdfPTable(2);
        table.setWidths(new int[]{80, 20});
        Stream.of("Descripción", "Cantidad")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setPadding(5);
                    table.addCell(header);
                });

        var teamBalance = teamMovementService.get().stream().mapToDouble(MovementDto::amount).sum();

        PdfPCell teamBalanceCell = new PdfPCell();
        teamBalanceCell.setPadding(5);
        teamBalanceCell.setPhrase(new Phrase("Balance general de la peña (balance temporada anterior + gastos generales)"));
        table.addCell(teamBalanceCell);

        PdfPCell totalBalanceAmountCell = new PdfPCell();
        totalBalanceAmountCell.setPadding(5);
        totalBalanceAmountCell.setPhrase(new Phrase(String.valueOf(teamBalance)));
        totalBalanceAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalBalanceAmountCell);

        PdfPCell membersBalanceCell = new PdfPCell();
        membersBalanceCell.setPadding(5);
        membersBalanceCell.setPhrase(new Phrase("Ingresos por cuotas y multas"));
        table.addCell(membersBalanceCell);

        PdfPCell membersTotalBalanceAmountCell = new PdfPCell();
        membersTotalBalanceAmountCell.setPadding(5);
        membersTotalBalanceAmountCell.setPhrase(new Phrase(String.valueOf(movementService.getTotalBalance().totalIncomes())));
        membersTotalBalanceAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(membersTotalBalanceAmountCell);

        PdfPCell totalBalanceCell = new PdfPCell();
        totalBalanceCell.setPadding(5);
        totalBalanceCell.setPhrase(new Phrase("Total", headerMemberFont));
        table.addCell(totalBalanceCell);

        var teamTotalBalance = teamMovementService.getTotalBalance();

        PdfPCell totalTeamBalanceAmountCell = new PdfPCell();
        totalTeamBalanceAmountCell.setPadding(5);
        totalTeamBalanceAmountCell.setPhrase(new Phrase(String.valueOf(teamTotalBalance.totalIncomes() + teamTotalBalance.totalExpenses()),headerMemberFont));
        totalTeamBalanceAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalTeamBalanceAmountCell);

        document.add(table);
    }

    private void addMembersBalanceTable( Document document) throws DocumentException {
    Paragraph membersBalance =
        new Paragraph("\n\nGastos e ingresos por cada miembro\n\n", paragraphFont);
        membersBalance.setAlignment(Element.ALIGN_CENTER);
        document.add(membersBalance);

        PdfPTable table = new PdfPTable(3);
        table.setWidths(new int[]{20, 65, 15});
        Stream.of("Fecha", "Descripción", "Cantidad")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setPadding(5);
                    table.addCell(header);
                });

        memberService
        .get()
        .forEach(
            member -> {
              PdfPCell header = new PdfPCell();
              header.setPhrase(
                  new Phrase(member.name() + " " + member.surname(), headerMemberFont));
              header.setPadding(5);
              header.setColspan(3);
              header.setHorizontalAlignment(Element.ALIGN_CENTER);
              table.addCell(header);
              try {
                movementService
                    .getByMember(member.id())
                    .forEach(
                        movement -> {
                          LocalDate date = LocalDate.parse(movement.createdOn(), formatter);
                          String spanishDate = date.format(formatterToSpanish);

                          PdfPCell dateCell = new PdfPCell();
                          dateCell.setPadding(5);
                          dateCell.setPhrase(new Phrase(spanishDate));
                          dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                          table.addCell(dateCell);

                          PdfPCell descriptionCell = new PdfPCell();
                          descriptionCell.setPadding(5);
                          descriptionCell.setPhrase(new Phrase(movement.description()));
                          table.addCell(descriptionCell);

                          PdfPCell amountCell = new PdfPCell();
                          amountCell.setPadding(5);
                          amountCell.setPhrase(new Phrase(String.valueOf(movement.amount())));
                          amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                          table.addCell(amountCell);
                        });
              } catch (MemberNotFoundException e) {
                throw new RuntimeException(e);
              };

                PdfPCell totalCell = new PdfPCell();
                totalCell.setColspan(2);
                totalCell.setPadding(5);
                totalCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                totalCell.setBorderWidth(2);
                totalCell.setPhrase(new Phrase("Balance total de " + member.name() + " " + member.surname()));
                table.addCell(totalCell);

              double total = movementService.getBalance(member.id());

              PdfPCell totalAmountCell = new PdfPCell();
              totalAmountCell.setPadding(5);
              totalAmountCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
              totalAmountCell.setBorderWidth(2);
              totalAmountCell.setPhrase(new Phrase(String.valueOf(total)));
              totalAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
              table.addCell(totalAmountCell);
            });

        PdfPCell summary = new PdfPCell();
        summary.setPhrase(
                new Phrase("Resumen", headerMemberFont));
        summary.setPadding(5);
        summary.setColspan(3);
        summary.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(summary);

        var balance = movementService.getTotalBalance();

        PdfPCell totalCell = new PdfPCell();
        totalCell.setColspan(2);
        totalCell.setPadding(5);
        totalCell.setPhrase(new Phrase("Total cuotas y multas"));
        table.addCell(totalCell);

        PdfPCell totalAmountCell = new PdfPCell();
        totalAmountCell.setPadding(5);
        totalAmountCell.setPhrase(new Phrase(String.valueOf(balance.totalExpenses() * -1)));
        totalAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalAmountCell);

        PdfPCell totalPaidCell = new PdfPCell();
        totalPaidCell.setColspan(2);
        totalPaidCell.setPadding(5);
        totalPaidCell.setPhrase(new Phrase("Total cuotas y multas pagadas"));
        table.addCell(totalPaidCell);

        PdfPCell totalPaidAmountCell = new PdfPCell();
        totalPaidAmountCell.setPadding(5);
        totalPaidAmountCell.setPhrase(new Phrase(String.valueOf(balance.totalIncomes())));
        totalPaidAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalPaidAmountCell);
        document.add(table);

        PdfPCell totalNoPaidCell = new PdfPCell();
        totalNoPaidCell.setColspan(2);
        totalNoPaidCell.setPadding(5);
        totalNoPaidCell.setPhrase(new Phrase("Total cuotas y multas sin pagar"));
        table.addCell(totalNoPaidCell);

        PdfPCell totalNoPaidAmountCell = new PdfPCell();
        totalNoPaidAmountCell.setPadding(5);
        totalNoPaidAmountCell.setPhrase(new Phrase(String.valueOf(balance.totalIncomes() + balance.totalExpenses())));
        totalNoPaidAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalNoPaidAmountCell);
        document.add(table);
    }

    private void addTeamBalanceTable( Document document) throws DocumentException {
    Paragraph teamBalance =
        new Paragraph("\n\nGastos e ingresos generales de la peña\n\n", paragraphFont);
        teamBalance.setAlignment(Element.ALIGN_CENTER);
        document.add(teamBalance);

        PdfPTable table = new PdfPTable(3);
        table.setWidths(new int[]{20, 65, 15});
        Stream.of("Fecha", "Descripción", "Cantidad")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setPadding(5);
                    table.addCell(header);
                });

        teamMovementService.get().forEach(movement -> {
            LocalDate date = LocalDate.parse(movement.createdOn(),formatter);
            String spanishDate = date.format(formatterToSpanish);

            PdfPCell dateCell = new PdfPCell();
            dateCell.setPadding(5);
            dateCell.setPhrase(new Phrase(spanishDate));
            dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(dateCell);

            PdfPCell descriptionCell = new PdfPCell();
            descriptionCell.setPadding(5);
            descriptionCell.setPhrase(new Phrase(movement.description()));
            table.addCell(descriptionCell);

            PdfPCell amountCell = new PdfPCell();
            amountCell.setPadding(5);
            amountCell.setPhrase(new Phrase(String.valueOf(movement.amount())));
            amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(amountCell);
        });

        PdfPCell totalCell = new PdfPCell();
        totalCell.setColspan(2);
        totalCell.setPadding(5);
        totalCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalCell.setBorderWidth(2);
        totalCell.setPhrase(new Phrase("Total"));
        table.addCell(totalCell);

        double total = teamMovementService.get().stream().mapToDouble(MovementDto::amount).sum();

        PdfPCell totalAmountCell = new PdfPCell();
        totalAmountCell.setPadding(5);
        totalAmountCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalAmountCell.setBorderWidth(2);
        totalAmountCell.setPhrase(new Phrase(String.valueOf(total)));
        totalAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalAmountCell);

        document.add(table);
    }
}
