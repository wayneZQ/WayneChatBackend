package com.polyu.im_bird_sys.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

@Component
public class QRcodeUtils {


    public void createQRcode(String filePath,String content){
        int width = 300;        //图片的宽
        int height = 300;       //图片的高
        String format = "png";

        /**
         * 定义二维码的参数
         */
        HashMap hints = new HashMap();
        hints.put(EncodeHintType.CHARACTER_SET,"utf-8");        //字符编码为utf-8
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);     //二维码的纠错等级为medium
        hints.put(EncodeHintType.MARGIN,2);     //设置图片的边距

        /**
         * 生成二维码
         */
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,width,height,hints);
            Path file = new File(filePath).toPath();
            MatrixToImageWriter.writeToPath(bitMatrix,format,file);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getContentFromQRCode(String filePath) {
        MultiFormatReader formatReader=new MultiFormatReader();
        File file=new File(filePath);
        BufferedImage image;
        try {
            image = ImageIO.read(file);
            BinaryBitmap binaryBitmap=new BinaryBitmap(new HybridBinarizer
                    (new BufferedImageLuminanceSource(image)));
            HashMap hints=new HashMap();
            hints.put(EncodeHintType.CHARACTER_SET,"utf-8");    //指定字符编码为“utf-8”
            Result result=formatReader.decode(binaryBitmap,hints);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
