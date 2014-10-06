package system;
import log.PukkaLogger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/****************************************************
 *
 *          Helper class for password encryption
 *
 *          Taken from:
 *          http://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
 *
 *
 */

public class PasswordManager {

    /******************************************************'
     *
     *          Authentication
     *
     * @param attemptedPassword - entered by the user
     * @param encryptedPassword - from database
     * @param salt - also from database
     *
     * @return - true if the passwords match
     *
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */


	 public boolean authenticate(String attemptedPassword,
                                 byte[] encryptedPassword,
                                 byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {

          // Encrypt the clear-text password using the same salt that was used to
          // encrypt the original password


         byte[] encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);

          // Authentication succeeds if encrypted password that the user entered
          // is equal to the stored hash

         if(Arrays.equals(encryptedPassword, encryptedAttemptedPassword))
             return true;

         PukkaLogger.log(PukkaLogger.Level.INFO, "Pwd did not match.");

         return false;
	 }


    /*************************************************************************************
     *
     *          Generate the encrypted password that will be stored in the database
     *
     * @param password
     * @param salt
     * @return
     *
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */


	 public byte[] getEncryptedPassword(String password,
                                        byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {

          // PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
          // specifically names SHA-1 as an acceptable hashing algorithm for PBKDF2

          String algorithm = "PBKDF2WithHmacSHA1";

          // SHA-1 generates 160 bit hashes, so that's what makes sense here
          int derivedKeyLength = 160;

          // Pick an iteration count that works for you. The NIST recommends at
          // least 1,000 iterations:
          // http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
          // iOS 4.x reportedly uses 10,000:
          // http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/

          int iterations = 20000;

          KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);
          SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

          return f.generateSecret(spec).getEncoded();
	 }

    /*********************************************************************************
     *
     *      Generate the random salt
     *
     *      A salt is a randomly generated sequence of bits that is unique to each user
     *      and is added to the user’s password as part of the hashing. This prevents
     *      "rainbow table" attacks by making a precomputed list of results unfeasible.
     *
     * @return - salt to be stored in the database together with the password
     * @throws NoSuchAlgorithmException
     *
     * 	  NOTE VERY important to use SecureRandom instead of just Random
     */

	 public byte[] generateSalt() throws NoSuchAlgorithmException {

          SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

          // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
          byte[] salt = new byte[8];
          random.nextBytes(salt);

          return salt;
	 }

}