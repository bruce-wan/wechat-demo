package io.example.wechat.core.util;

import io.example.wechat.core.error.CodecException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.*;

/**
 * Created by bruce.wan on 2021/1/5.
 */
public class CodecSupport
{
  public static final String PREFERRED_ENCODING = "UTF-8";

  public static byte[] toBytes(char[] chars)
  {
    return toBytes(new String(chars), PREFERRED_ENCODING);
  }

  public static byte[] toBytes(char[] chars, String encoding) throws CodecException
  {
    return toBytes(new String(chars), encoding);
  }

  public static byte[] toBytes(String source)
  {
    return toBytes(source, PREFERRED_ENCODING);
  }

  public static byte[] toBytes(String source, String encoding) throws CodecException
  {
    try
    {
      return source.getBytes(encoding);
    }
    catch(UnsupportedEncodingException e)
    {
      String msg = "Unable to convert source [" + source + "] to byte array using " + "encoding '" + encoding + "'";
      throw new CodecException(msg, e);
    }
  }

  public static byte[] toBytes(File file) throws CodecException
  {
    if (file == null) {
      throw new IllegalArgumentException("File argument cannot be null.");
    }
    try {
      return toBytes(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      String msg = "Unable to acquire InputStream for file [" + file + "]";
      throw new CodecException(msg, e);
    }
  }

  public static byte[] toBytes(InputStream inputStream) throws CodecException
  {
    if (inputStream == null) {
      throw new IllegalArgumentException("InputStream argument cannot be null.");
    }
    final int BUFFER_SIZE = 512;
    ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    try {
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
      return out.toByteArray();
    } catch (IOException ioe) {
      throw new CodecException("Unable to read inputstream", ioe);
    } finally {
      try {
        inputStream.close();
      } catch (IOException ignored) {
      }
      try {
        out.close();
      } catch (IOException ignored) {
      }
    }
  }

  public static byte[] toBytes(Object source)
  {
    if(source == null)
    {
      String msg = "Argument for byte conversion cannot be null.";
      throw new IllegalArgumentException(msg);
    }
    if(source instanceof byte[])
    {
      return (byte[])source;
    }
    else if(source instanceof char[])
    {
      return toBytes((char[])source);
    }
    else if(source instanceof String)
    {
      return toBytes((String)source);
    }
    else if(source instanceof File)
    {
      return toBytes((File)source);
    }
    else if(source instanceof InputStream)
    {
      return toBytes((InputStream)source);
    }
    else
    {
      throw new CodecException(ErrorCode.COMMON_ERROR, "Can not support convert " + source.getClass().getName() + " to byte[].");
    }
  }

  public static String toString(byte[] bytes)
  {
    return toString(bytes, PREFERRED_ENCODING);
  }

  public static String toString(byte[] bytes, String encoding) throws CodecException
  {
    try
    {
      return new String(bytes, encoding);
    }
    catch(UnsupportedEncodingException e)
    {
      String msg = "Unable to convert byte array to String with encoding '" + encoding + "'.";
      throw new CodecException(msg, e);
    }
  }

  public static char[] toChars(byte[] bytes)
  {
    return toChars(bytes, PREFERRED_ENCODING);
  }

  public static char[] toChars(byte[] bytes, String encoding) throws CodecException
  {
    return toString(bytes, encoding).toCharArray();
  }

  public static String encodeHex(byte[] data) {
    return Hex.encodeHexString(data);
  }

  public static byte[] decodeHex(String data)
  {
    try
    {
      return Hex.decodeHex(data);
    }
    catch(Exception e)
    {
      String msg = "Unable to convert hex string to byte array with encoding.";
      throw new CodecException(msg, e);
    }
  }

  public static String encodeBase64(byte[] data) {
    return Base64.encodeBase64String(data);
  }

  public static byte[] decodeBase64(String data) {
    return Base64.decodeBase64(data);
  }
}
