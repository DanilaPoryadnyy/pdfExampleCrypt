package org.example;

import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import ru.CryptoPro.JCP.JCP;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.HashMap;

public class PDFSignatureExample {
    private static final char[] STORE_PASS = "password".toCharArray();
    private static final String STORE_TYPE = "HDImageStore";
    private static final String keystorePath = "/home/certificates/storage_2012_2567.keystore";
    private static final String alias = "certificate_test_2012_2567";
    private static final String filePath = "C:\\Users\\DanilaLC\\Downloads\\sample-pdf-file.pdf";
    private static final String fileOut = "C:\\Users\\DanilaLC\\Downloads\\output.pdf";

    public static void main(String[] args) throws Exception {
        KeyStore keystore = KeyStore.getInstance(STORE_TYPE);

        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keystore.load(fis, STORE_PASS);
        }

        PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, STORE_PASS);
        Certificate certificate = keystore.getCertificate(alias);

        String hashAlgorithm = null;
        if (privateKey.getAlgorithm().equals(JCP.GOST_EL_2012_256_NAME) ||
                privateKey.getAlgorithm().equals(JCP.GOST_DH_2012_256_NAME)) {
            hashAlgorithm = JCP.GOST_DIGEST_2012_256_NAME;
        }
        else if (
                privateKey.getAlgorithm().equals(JCP.GOST_EL_2012_512_NAME) ||
                        privateKey.getAlgorithm().equals(JCP.GOST_DH_2012_512_NAME)) {
            hashAlgorithm = JCP.GOST_DIGEST_2012_512_NAME;
        }

        sign(privateKey, hashAlgorithm, "JCP", new Certificate[]{certificate},
                filePath,
                fileOut,
                "Moscow",
                "Some Reason",
                true);
    }

    public static void sign(PrivateKey privateKey, String hashAlgorithm,
                            String signProvider, Certificate[] chain, String fileToSign,
                            String signedFile, String location, String reason, boolean append)
            throws Exception {

        PdfReader reader = new PdfReader(fileToSign);
        try (FileOutputStream fout = new FileOutputStream(signedFile)) {
            PdfStamper stp = append ? PdfStamper.createSignature(reader, fout, '\0', null, true) : PdfStamper.createSignature(reader, fout, '\0');

            PdfSignatureAppearance sap = stp.getSignatureAppearance();

            sap.setCertificate(chain[0]);
            sap.setReason(reason);
            sap.setLocation(location);

            PdfSignature dic = new PdfSignature(PdfName.ADOBE_CryptoProPDF, PdfName.ADBE_PKCS7_DETACHED);

            dic.setReason(sap.getReason());
            dic.setLocation(sap.getLocation());
            dic.setSignatureCreator(sap.getSignatureCreator());
            dic.setContact(sap.getContact());
            dic.setDate(new PdfDate(sap.getSignDate()));

            sap.setCryptoDictionary(dic);
            int estimatedSize = 8192;

            HashMap<PdfName, Integer> exc = new HashMap<>();
            exc.put(PdfName.CONTENTS, estimatedSize * 2 + 2);

            PdfPKCS7 sgn = new PdfPKCS7(privateKey, chain,
                    hashAlgorithm, signProvider, null, false);

            sap.preClose(exc);

            byte[] hash = DigestAlgorithms.digest(sap.getRangeStream(),MessageDigest.getInstance(hashAlgorithm));

            Calendar cal = Calendar.getInstance();

            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, cal,
                    null, null, MakeSignature.CryptoStandard.CMS);

            sgn.update(sh, 0, sh.length);
            byte[] encodedSig = sgn.getEncodedPKCS7(hash, cal);

            byte[] paddedSig = new byte[estimatedSize];
            System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);

            PdfDictionary dic2 = new PdfDictionary();
            dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));

            sap.close(dic2);
            stp.close();
        }
    }
}