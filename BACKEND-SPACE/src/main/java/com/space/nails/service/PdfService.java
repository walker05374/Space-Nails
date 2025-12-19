package com.space.nails.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.space.nails.model.Agendamento;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {

    public byte[] gerarReciboPDF(Agendamento agendamento) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A6); // A6 é bom para recibo mobile
            PdfWriter.getInstance(document, out);
            document.open();

            // Fontes
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // Cabeçalho
            Paragraph titulo = new Paragraph("RECIBO DE SERVIÇO", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" ", normalFont)); // Espaço

            // Detalhes do Profissional (Quem prestou o serviço)
            document.add(new Paragraph("Profissional: " + agendamento.getProfissional().getNome(), boldFont));
            document.add(new Paragraph("------------------------------------------------", normalFont));

            // Detalhes do Cliente
            document.add(new Paragraph("Cliente: " + agendamento.getCliente().getNome(), normalFont));
            
            // Detalhes do Serviço
            document.add(new Paragraph("Serviço: " + agendamento.getServico().getNome(), normalFont));
            
            // Data e Hora
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            document.add(new Paragraph("Data: " + agendamento.getDataHora().format(fmt), normalFont));

            document.add(new Paragraph(" ", normalFont));

            // Valor
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            String valorFormatado = nf.format(agendamento.getServico().getValor());
            
            Paragraph valorPara = new Paragraph("TOTAL: " + valorFormatado, tituloFont);
            valorPara.setAlignment(Element.ALIGN_RIGHT);
            document.add(valorPara);

            document.add(new Paragraph(" ", normalFont));
            document.add(new Paragraph("Nota não fiscal - Gerada pelo Space Nails", FontFactory.getFont(FontFactory.HELVETICA, 8)));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }
}