package ptolemy.actor.lib.jjs.modules.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

//import javax.crypto.Cipher;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;
import ptolemy.data.UnsignedByteToken;
import ptolemy.kernel.util.IllegalActionException;

/** Helper for the crypto JavaScript module. 

 *  @author Hokeun Kim
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class CryptoHelper extends HelperBase {

    /** Constructor for CryptoHelper.
     *  @param currentObj The JavaScript object that this is helping.
     */
    public CryptoHelper(ScriptObjectMirror currentObj) {
        super(currentObj);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Return an array of camera names that are available on this host.
     *  This method refreshes the list.
     *  @return A list of camera names, or null if there none.
     *  @throws IllegalActionException 
     */
    public Object randomBytes(int size) throws IllegalActionException {
        // generate initialization vector
        return _toJSArray(_getRandomBytes(size));
    }
    
    /** Return an array of camera names that are available on this host.
     *  This method refreshes the list.
     *  @return A list of camera names, or null if there none.
     *  @throws IllegalActionException 
     */
    public Object symmetricEncrypt(Object input, Object key, String cipherAlgorithm)
            throws IllegalActionException {
        System.out.println(input);
        System.out.println(input.getClass());
        Cipher cipher = _getCipher(Cipher.ENCRYPT_MODE, cipherAlgorithm, _toJavaBytes(key), null);
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        try {
            // write initialization vector first
            byte[] initVector = cipher.getIV();
            if (initVector != null) {
                byteArrayOutputStream.write(initVector);
            }
            byteArrayOutputStream.write(cipher.doFinal(_toJavaBytes(input)));
        }
        catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new IllegalArgumentException("Problem processing " + input + "\n" + e.getMessage());
        }
        return _toJSArray(byteArrayOutputStream.toByteArray());
    }
    
    public Object symmetricDecrypt(Object input, Object key, String cipherAlgorithm)
            throws IllegalActionException {
        byte[] cipherText = _toJavaBytes(input);
        Cipher cipher = _getCipher(Cipher.DECRYPT_MODE, cipherAlgorithm, _toJavaBytes(key), cipherText);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        int ivSize = 0;
        if (cipher.getIV() != null) {
            ivSize = cipher.getIV().length;
        }
        try {
            byteArrayOutputStream.write(
                    cipher.doFinal(cipherText, ivSize, cipherText.length - ivSize));
        } catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new IllegalArgumentException("Problem processing " + input + "\n" + e.getMessage());
        }
        return _toJSArray(byteArrayOutputStream.toByteArray());
    }
    
    public Object hash(Object input, String hashAlgorithm) throws IllegalActionException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            return _toJSArray(messageDigest.digest(_toJavaBytes(input)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalActionException("Failed to initialize messageDigest.\n" + e.getMessage());
        }
    }
    
    public boolean verifyHash(Object input, String hashAlgorithm) throws IllegalActionException {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalActionException("Failed to initialize messageDigest.\n" + e.getMessage());
        }
        byte[] bytes = _toJavaBytes(input);
        if (bytes.length < messageDigest.getDigestLength()) {
            throw new IllegalActionException("Input is shorter than expected hash");
        }
        byte[] givenHash = Arrays.copyOfRange(bytes, bytes.length - messageDigest.getDigestLength(), bytes.length);
        
        messageDigest.update(bytes, 0, bytes.length - messageDigest.getDigestLength());
        byte[] computedHash = messageDigest.digest();
        
        return Arrays.equals(givenHash, computedHash);
    }

    public int getHashLength(String hashAlgorithm) throws IllegalActionException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            return messageDigest.getDigestLength();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalActionException("Failed to initialize messageDigest.\n" + e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    private Object _toJSArray(byte[] buffer) throws IllegalActionException
    {
        Object[] result = new Object[buffer.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Object)buffer[i];
        }
        return _actor.toJSArray(result);
    }
    
    private byte[] _toJavaBytes(Object object) {
        if (object instanceof ScriptObjectMirror) {
            ScriptObjectMirror objectMirror = ((ScriptObjectMirror)object);

            byte[] result = new byte[objectMirror.size()];
            int i = 0;
            for (Object value : objectMirror.values()) {
                if (value instanceof UnsignedByteToken) {
                    result[i] = ((UnsignedByteToken) value).byteValue();
                }
                else if (value instanceof Integer) {
                    result[i] = ((Integer) value).byteValue();
                }
                i++;
            }
            return result;
        }
        else if (object instanceof String) {
            String stringObject = (String)object;
            if (stringObject.startsWith("0x")) {
                // hex encoded string
                return DatatypeConverter.parseHexBinary(stringObject.substring(2));
            }
            else {
                return ((String)object).getBytes();
            }
        }
        return null;
    }

    /** Return an array of camera names that are available on this host.
     *  This method refreshes the list.
     *  @param operationMode Whether it is encrypt or decrypt operation.
     *  @return A list of camera names, or null if there none.
     *  @throws IllegalArgumentException 
     */
    private Cipher _getCipher(int operationMode, String cipherAlgorithm,
            byte[] cipherKey, byte[] cipherText) throws IllegalArgumentException {
        String[] tokens = cipherAlgorithm.split("-");
        if (tokens.length < 1) {
            throw new IllegalArgumentException("Invalid cipher algorithm: " + cipherAlgorithm);
        }
        int keySize = 0;
        int blockSize = 0;
        int ivSize = 0;
        String cipherName = "";
        String cipherMode = "";
        
        if (tokens[0].equals("AES")) {
            cipherName = tokens[0];
            blockSize = 16;
            
            if (tokens.length != 3) {
                throw new IllegalArgumentException("Invalid cipher algorithm: " + cipherAlgorithm);
            }
            
            if (tokens[1].equals("128")) {
                keySize = 16;
            }
            else if (tokens[1].equals("192")) {
                keySize = 24;
            }
            else if (tokens[1].equals("256")) {
                keySize = 32;
            }
            else {
                throw new IllegalArgumentException("Invalid key size: " + cipherAlgorithm);
            }

            if (tokens[2].equals("ECB")) {
                cipherMode = tokens[2];
            }
            else if (tokens[2].equals("CBC") || tokens[2].equals("CFB")) {
                cipherMode = tokens[2];
                ivSize = blockSize;
            }
            else {
                throw new IllegalArgumentException("Invalid cipher mode: " + cipherAlgorithm);
            }
        }
        else if (tokens[0].equals("DES")) {
            // 8 bytes, each byte with 7bit key and 1 parity -> total 56bits key used
            cipherName = tokens[0];
            keySize = 8;
            blockSize = 8;
            
            if (tokens.length != 2) {
                throw new IllegalArgumentException("Invalid cipher algorithm: " + cipherAlgorithm);
            }
            
            if (tokens[1].equals("ECB")) {
                cipherMode = tokens[1];
            }
            else if (tokens[1].equals("CBC")) {
                cipherMode = tokens[1];
                ivSize = blockSize;
            }
            else {
                throw new IllegalArgumentException("Invalid cipher mode: " + cipherAlgorithm);
            }
        }
        else {
            throw new IllegalArgumentException("Invalid cipher: " + cipherAlgorithm);
        }
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(cipherName + "/" + cipherMode + "/PKCS5Padding");
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalArgumentException("Invalid cipher algorithm: " + cipherAlgorithm
                    + "\n" + e.getMessage());
        }
        
        IvParameterSpec ivSpec = null;
        byte[] initVector = null;
        if (ivSize > 0) {
            if (cipherText == null) {
                initVector = _getRandomBytes(ivSize);
            }
            else {
                // take first ivSize bytes as IV
                initVector = Arrays.copyOfRange(cipherText, 0, ivSize);
            }
            ivSpec = new IvParameterSpec(initVector);
        }
        
        if (cipherKey.length != keySize) {
            throw new IllegalArgumentException("Invalid cipher key length: " + cipherAlgorithm);
        }
        
        SecretKeySpec secretKeySpec = new SecretKeySpec(cipherKey, cipherName);

        try {
            if (ivSpec != null) {
                cipher.init(operationMode, secretKeySpec, ivSpec);
            }
            else {
                cipher.init(operationMode, secretKeySpec);
            }
        }
        catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("Invalid cipher algorithm: " + cipherAlgorithm
                    + "\n" + e.getMessage());
        }
        
        return cipher;
    }
    
    private byte[] _getRandomBytes(int size) {
        SecureRandom random = new SecureRandom();
        byte seed[] = random.generateSeed(size);
        byte[] randomBytes = new byte[size];
        random.setSeed(seed);
        random.nextBytes(randomBytes);
        return randomBytes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
}
