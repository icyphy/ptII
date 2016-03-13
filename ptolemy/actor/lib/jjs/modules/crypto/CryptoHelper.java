package ptolemy.actor.lib.jjs.modules.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;
import ptolemy.data.UnsignedByteToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;

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

    /** Return the expected hash length for the given hash algorithm.
     *  @param hashAlgorithm The name of the hash algorithm.
     *  @return The hash length for the hash algorithm.
     *  @throws IllegalActionException If the specified hash algorithm is not available.
     */
    public int getHashLength(String hashAlgorithm) throws IllegalActionException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            return messageDigest.getDigestLength();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalActionException("Failed to initialize messageDigest.\n" + e.getMessage());
        }
    }

    /** Hash the input with a given hash algorithm and return the hashed result.
     *  @param input The input in the JavaScript object to be hashed.
     *  @param hashAlgorithm The name of the hash algorithm to be used. (Examples: MD5, SHA-1, SHA-256)
     *  @return The hash digested for the given input.
     *  @throws IllegalActionException If the specified hash algorithm is not available.
     */
    public Object hash(Object input, String hashAlgorithm) throws IllegalActionException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            return _toJSArray(messageDigest.digest(_toJavaBytes(input)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalActionException("Failed to initialize messageDigest.\n" + e.getMessage());
        }
    }
    
    /** Calculate HMAC (key-Hashed Message Authentication Code) for the given input and key.
     *  @param input The input in the JavaScript object to be HMAC hashed.
     *  @param key The key to be used for HMAC calculation.
     *  @param hmacAlgorithm The name of the HMAC algorithm.
     *  @return The resulting HMAC. 
     *  @throws IllegalActionException If the HMAC calculation fails.
     */
    public Object hmac(Object input, Object key, String hmacAlgorithm) throws IllegalActionException {
        try {
            SecretKeySpec hmacKey = new SecretKeySpec(_toJavaBytes(key), hmacAlgorithm);
            Mac hmac = Mac.getInstance(hmacAlgorithm);
            hmac.init(hmacKey);
            return _toJSArray(hmac.doFinal(_toJavaBytes(input)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalActionException("Failed to calculate hmac.\n" + e.getMessage());
        }
    }

    /** Load and return a private key from a RSA private key file in DER format.
     *  @param filePath The path for the file that stores a RSA private key in DER format.
     *  @return PrivateKey object loaded from the file.
     *  @throws IllegalActionException If there is a problem with loading the private key.
     */
    public PrivateKey loadPrivateKey(String filePath) throws IllegalActionException {
        if (!filePath.endsWith(".der")) {
            throw new IllegalArgumentException("Private key should be in DER format. " + filePath);
        }
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(_readBinaryFile(filePath));
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("Problem loading private key " + filePath + "\n" + e.getMessage());
        }
    }

    /** Load and return a public key from a X.509 certificate file in PEM format.
     *  @param filePath The path for the file that stores a X.509 certificate.
     *  @return PublicKey object loaded from the certificate.
     *  @throws IllegalActionException If there is a problem with loading the public key.
     */
    public PublicKey loadPublicKey(String filePath) throws IllegalActionException {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            File file = FileUtilities.nameToFile(filePath, null);
            FileInputStream inStream = new FileInputStream(file);
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inStream);
            return cert.getPublicKey();
        } catch (CertificateException | FileNotFoundException e) {
            throw new IllegalArgumentException("Problem loading public key " + filePath + "\n" + e.getMessage());
        }
    }

    /** Decrypt the input with an asymmetric cipher private key and return the decrypted result.
     *  @param input The cipher text to be decrypted.
     *  @param privateKey The private key of the destination entity.
     *  @param cipherAlgorithm The name of the asymmetric cipher to be used for decryption.
     *  @return The decrypted result in a JavaScript integer array.
     *  @throws IllegalActionException If the private key decryption fails.
     */
    public Object privateDecrypt(Object input, PrivateKey privateKey, String cipherAlgorithm)
            throws IllegalActionException {
        return _performAsymmetricCrypto(Cipher.DECRYPT_MODE, input, privateKey, cipherAlgorithm);
    }

    /** Encrypt the input with an asymmetric cipher public key and return the encrypted result.
     *  @param input The clear text message to be encrypted.
     *  @param publicKey The public key of the entity that will decrypt the message.
     *  @param cipherAlgorithm The name of the asymmetric cipher to be used for encryption.
     *  @return The encrypted result in a JavaScript integer array.
     *  @throws IllegalActionException If the public key encryption fails.
     */
    public Object publicEncrypt(Object input, PublicKey publicKey, String cipherAlgorithm)
            throws IllegalActionException {
        return _performAsymmetricCrypto(Cipher.ENCRYPT_MODE, input, publicKey, cipherAlgorithm);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Return an array of randomly generated bytes.
     *  @param size The number of bytes to be generated.
     *  @return A JavaScript integer array that is randomly generated.
     *  @throws IllegalActionException If the generated bytes cannot be converted to JavaScript array.
     */
    public Object randomBytes(int size) throws IllegalActionException {
        // generate initialization vector
        return _toJSArray(_getRandomBytes(size));
    }

    /** Sign the given input data with a private key and return the signature.
     *  @param input The input data to be signed in a JavaScript object.
     *  @param privateKey The public key of the entity that will decrypt the message.
     *  @param signAlgorithm The name of the algorithm to be used for signing.
     *  @return Signature calculated from the input data.
     *  @throws IllegalActionException
     */
    public Object signWithPrivateKey(Object input, PrivateKey privateKey, String signAlgorithm)
            throws IllegalActionException {
        try {
            Signature signer = Signature.getInstance(signAlgorithm);
            signer.initSign(privateKey); // cf) initVerify
            signer.update(_toJavaBytes(input));
            return _toJSArray(signer.sign());
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IllegalArgumentException("Problem signing with private key\n" + e.getMessage());
        }
    }

    /** Decrypt the input with a symmetric cipher and return the decrypted result.
     *  @param input The cipher text to be decrypted.
     *  @param key The secret key for the cipher.
     *  @param cipherAlgorithm The name of the symmetric cipher algorithm to be used for decryption. (examples: AES-128-CBC, DES-ECB)
     *  @return The encrypted result in JavaScript byte array.
     *  @throws IllegalActionException If the decryption fails.
     */
    public Object symmetricDecrypt(Object input, Object key, String cipherAlgorithm) throws IllegalActionException {
        byte[] cipherText = _toJavaBytes(input);
        Cipher cipher = _getCipher(Cipher.DECRYPT_MODE, cipherAlgorithm, _toJavaBytes(key), cipherText);
    
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    
        int ivSize = 0;
        if (cipher.getIV() != null) {
            ivSize = cipher.getIV().length;
        }
        try {
            byteArrayOutputStream.write(cipher.doFinal(cipherText, ivSize, cipherText.length - ivSize));
        } catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new IllegalArgumentException("Problem processing " + input + "\n" + e.getMessage());
        }
        return _toJSArray(byteArrayOutputStream.toByteArray());
    }

    /** Encrypt the input with a symmetric cipher and return the encrypted result.
     *  @param input The clear text message to be encrypted.
     *  @param key The secret key for the cipher.
     *  @param cipherAlgorithm The name of the symmetric cipher algorithm to be used for encryption. (examples: AES-128-CBC, DES-ECB)
     *  @return The encrypted result in JavaScript byte array.
     *  @throws IllegalActionException If the encryption fails.
     */
    public Object symmetricEncrypt(Object input, Object key, String cipherAlgorithm) throws IllegalActionException {
        Cipher cipher = _getCipher(Cipher.ENCRYPT_MODE, cipherAlgorithm, _toJavaBytes(key), null);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            // write initialization vector first
            byte[] initVector = cipher.getIV();
            if (initVector != null) {
                byteArrayOutputStream.write(initVector);
            }
            byteArrayOutputStream.write(cipher.doFinal(_toJavaBytes(input)));
        } catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new IllegalArgumentException("Problem processing " + input + "\n" + e.getMessage());
        }
        return _toJSArray(byteArrayOutputStream.toByteArray());
    }

    /** Verify the signature for given input data and public key of the signer.
     *  @param data The input data to be verified.
     *  @param signature The signature to be verified.
     *  @param publicKey The public key to be used for signature verification.
     *  @param signAlgorithm The name of the algorithm to be used for signature verification.
     *  @return Whether the signature is valid.
     *  @throws IllegalArgumentException
     */
    public boolean verifySignature(Object data, Object signature, PublicKey publicKey, String signAlgorithm)
            throws IllegalArgumentException {
        Signature verifier;
        try {
            verifier = Signature.getInstance(signAlgorithm);
            verifier.initVerify(publicKey);
            verifier.update(_toJavaBytes(data));
            return verifier.verify(_toJavaBytes(signature));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IllegalActionException e) {
            throw new IllegalArgumentException("Problem verifying signature\n" + e.getMessage());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Initialize and return a Java Cipher object for the specified operation mode, cipher algorithm,
     *  cipher key and initialization vector (if applicable) in the prefix of the cipher text.
     *  @param operationMode Whether to encrypt or decrypt. (Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE)
     *  @param cipherAlgorithm The name of the symmetric cipher algorithm to be used. (examples: AES-128-CBC, DES-ECB)
     *  @param cipherKey The secret key for the cipher.
     *  @param cipherText The cipher text that includes the initialization vector (IV). Should be null
     *    if the operation mode is encrypt or the cipher algorithm does not use IV. 
     *  @return An initialized Java Cipher object.
     *  @throws IllegalArgumentException If the Java Cipher object cannot be initialized.
     */
    private Cipher _getCipher(int operationMode, String cipherAlgorithm, byte[] cipherKey, byte[] cipherText)
            throws IllegalArgumentException {
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
            } else if (tokens[1].equals("192")) {
                keySize = 24;
            } else if (tokens[1].equals("256")) {
                keySize = 32;
            } else {
                throw new IllegalArgumentException("Invalid key size: " + cipherAlgorithm);
            }
    
            if (tokens[2].equals("ECB")) {
                cipherMode = tokens[2];
            } else if (tokens[2].equals("CBC") || tokens[2].equals("CFB")) {
                cipherMode = tokens[2];
                ivSize = blockSize;
            } else {
                throw new IllegalArgumentException("Invalid cipher mode: " + cipherAlgorithm);
            }
        } else if (tokens[0].equals("DES")) {
            // 8 bytes, each byte with 7bit key and 1 parity -> total 56bits key used
            cipherName = tokens[0];
            keySize = 8;
            blockSize = 8;
    
            if (tokens.length != 2) {
                throw new IllegalArgumentException("Invalid cipher algorithm: " + cipherAlgorithm);
            }
    
            if (tokens[1].equals("ECB")) {
                cipherMode = tokens[1];
            } else if (tokens[1].equals("CBC")) {
                cipherMode = tokens[1];
                ivSize = blockSize;
            } else {
                throw new IllegalArgumentException("Invalid cipher mode: " + cipherAlgorithm);
            }
        } else {
            throw new IllegalArgumentException("Invalid cipher: " + cipherAlgorithm);
        }
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(cipherName + "/" + cipherMode + "/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalArgumentException("Invalid cipher algorithm: " + cipherAlgorithm + "\n" + e.getMessage());
        }
    
        IvParameterSpec ivSpec = null;
        byte[] initVector = null;
        if (ivSize > 0) {
            if (cipherText == null) {
                initVector = _getRandomBytes(ivSize);
            } else {
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
            } else {
                cipher.init(operationMode, secretKeySpec);
            }
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("Invalid cipher algorithm: " + cipherAlgorithm + "\n" + e.getMessage());
        }
    
        return cipher;
    }

    /** Internal function for generating random bytes.
     *  @param size The number of bytes to be generated.
     *  @return A Java byte array with randomly generated bytes.
     */
    private byte[] _getRandomBytes(int size) {
        SecureRandom random = new SecureRandom();
        byte seed[] = random.generateSeed(size);
        byte[] randomBytes = new byte[size];
        random.setSeed(seed);
        random.nextBytes(randomBytes);
        return randomBytes;
    }

    /** Perform asymmetric cryptography operation (encrypt or decrypt) and return the result.
     *  @param operationMode Whether to encrypt or decrypt. (Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE)
     *  @param input The input data (clear text or cipher text) to be operated.
     *  @param key The asymmetric cipher key to be used.
     *  @param cipherAlgorithm The name of the symmetric cipher algorithm to be used. (Examples: RSA/ECB/PKCS1PADDING)
     *  @return The cryptography operation result
     *  @throws IllegalActionException If the cryptography operation fails.
     */
    private Object _performAsymmetricCrypto(int operationMode, Object input, Key key, String cipherAlgorithm)
            throws IllegalActionException {
        Cipher cipher = null;
        try {
            // RSA/NONE/OAEPPadding =? Cipher.RSA_PKCS1_OAEP_PADDING
            //cipher = Cipher.getInstance("RSA/NONE/OAEPPadding");
            cipher = Cipher.getInstance(cipherAlgorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalArgumentException("Problem getting instance " + input + "\n" + e.getMessage());
        }
    
        try {
            cipher.init(operationMode, key);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Problem with key " + input + "\n" + e.getMessage());
        }
    
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(cipher.doFinal(_toJavaBytes(input)));
        } catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new IllegalArgumentException("Problem processing crypto " + input + "\n" + e.getMessage());
        }
        return _toJSArray(byteArrayOutputStream.toByteArray());
    }

    /** Read a binary file and return a Java byte array.
     *  @param filePath The path of the file to be read.
     *  @return A Java byte array with the file contents.
     */
    private byte[] _readBinaryFile(String filePath) throws IllegalActionException {
        File file = FileUtilities.nameToFile(filePath, null);
        FileInputStream fileInStream = null;
        try {
            fileInStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalActionException("File not found.\n" + e.getMessage());
        }
        DataInputStream dataInStream = new DataInputStream(fileInStream);
        byte[] bytes = new byte[(int) file.length()];
        try {
            dataInStream.readFully(bytes);
            dataInStream.close();
        } catch (IOException e) {
            throw new IllegalActionException("Exception while reading file.\n" + e.getMessage());
        }
        return bytes;
    }

    /** Convert a Java byte array into the JavaScript integer array.
     *  @param buffer The input Java byte array to be converted.
     *  @return The resulting JavaScript integer array.
     *  @throws IllegalActionException If the conversion fails.
     */
    private Object _toJSArray(byte[] buffer) throws IllegalActionException {
        Object[] result = new Object[buffer.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Object) buffer[i];
        }
        return _actor.toJSArray(result);
    }

    /** Convert the native JavaScript array or JavaScript string into a Java byte array.
     *  When the input is String that starts with "0x", interpret the input as hex bytes.
     *  @param object The input in JavaScript object, either a JavaScrypt integer array or
     *    a JavaScript string. If the string starts with "0x", it is interpreted as hex bytes.
     *  @return The Java byte array.
     *  @throws IllegalActionException If the conversion fails.
     */
    private byte[] _toJavaBytes(Object object) throws IllegalActionException {
        if (object instanceof ScriptObjectMirror) {
            ScriptObjectMirror objectMirror = ((ScriptObjectMirror) object);

            byte[] result = new byte[objectMirror.size()];
            int i = 0;
            for (Object value : objectMirror.values()) {
                if (value instanceof UnsignedByteToken) {
                    result[i] = ((UnsignedByteToken) value).byteValue();
                } else if (value instanceof Integer) {
                    result[i] = ((Integer) value).byteValue();
                }
                i++;
            }
            return result;
        } else if (object instanceof String) {
            String stringObject = (String) object;
            if (stringObject.startsWith("0x")) {
                // hex encoded string
                return DatatypeConverter.parseHexBinary(stringObject.substring(2));
            } else {
                return ((String) object).getBytes();
            }
        }
        throw new IllegalActionException("Cannot interpret the input, the input should be either"
                + "JavaScript int array or string.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
}
