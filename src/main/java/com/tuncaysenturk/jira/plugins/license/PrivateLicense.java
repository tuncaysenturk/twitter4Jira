package com.tuncaysenturk.jira.plugins.license;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;

public class PrivateLicense {
	private static Logger logger;
	protected Properties licenseProperties = null;
	private boolean verified = false;

	private PGPSecretKey key = null;
	private int hashAlgorithm = 2;

	byte[] publicKeyRing = null;

	private Long decodeKeyId = null;
	
	public boolean isDefined() {
		return (null != publicKeyRing && null != licenseProperties);
	}

	public void setFeature(String key, String value) {
		this.licenseProperties.put(key, value);
	}

	public String getFeature(String key) {
		if (this.licenseProperties == null) {
			return null;
		}
		if (this.licenseProperties.containsKey(key)) {
			return this.licenseProperties.getProperty(key);
		}
		return null;
	}

	public void setLicense(File file) throws IOException {
		this.verified = false;
		this.licenseProperties = new Properties();
		this.licenseProperties.load(new FileInputStream(file));
	}

	public void setLicense(String licenseString) throws IOException {
		this.verified = false;
		this.licenseProperties = new Properties();
		this.licenseProperties.load(new ByteArrayInputStream(licenseString
				.getBytes()));
	}

	public String getLicenseString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			this.licenseProperties.store(baos, "-- license file");
			return new String(baos.toByteArray());
		} catch (IOException ex) {
		}
		return "";
	}

	public void dumpLicense(String fileName) throws FileNotFoundException {
		dumpLicense(new File(fileName));
	}

	public void dumpLicense(File file) throws FileNotFoundException {
		dumpLicense(new FileOutputStream(file));
	}

	public void dumpLicense(OutputStream os) {
		this.licenseProperties.list(new PrintStream(os));
	}

	public boolean isVerified() {
		return this.verified;
	}

	public void setHashAlgorithm(int hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	public void loadKey(String fn, String userId) throws IOException,
			PGPException {
		loadKey(new File(fn), userId);
	}

	public void loadKeyRingFromResource(String resourceName, byte[] digest)
			throws Exception {
		loadKeyRing(
				PrivateLicense.class.getClassLoader().getResourceAsStream(
						resourceName), digest);
	}

	public void loadKeyRing(String fileName, byte[] digest) throws Exception,
			IOException {
		try {
			loadKeyRing(new File(fileName), digest);
		} catch (Exception e) {
			logger.error("Can not load key ring from the file '" + fileName
					+ "'", e);
		}
	}

	public void loadKeyRing(File file, byte[] digest) throws Exception {
		loadKeyRing(new FileInputStream(file), digest);
	}

	public void loadKeyRing(InputStream in, byte[] digest) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int ch;
		while ((ch = in.read()) >= 0) {
			baos.write(ch);
		}
		this.publicKeyRing = baos.toByteArray();
		if (digest != null) {
			byte[] calculatedDigest = calculatePublicKeyRingDigest();
			for (int i = 0; i < calculatedDigest.length; i++)
				if (calculatedDigest[i] != digest[i]) {
					this.publicKeyRing = null;
					throw new Exception("Key ring digest does not match.");
				}
		}
	}

	public byte[] calculatePublicKeyRingDigest() {
		SHA256Digest dig = new SHA256Digest();
		dig.reset();
		dig.update(this.publicKeyRing, 0, this.publicKeyRing.length);
		byte[] digest = new byte[32];
		dig.doFinal(digest, 0);
		return digest;
	}

	public String dumpPublicKeyRingDigest() {
		byte[] calculatedDigest = calculatePublicKeyRingDigest();
		String retval = "byte [] digest = new byte[] {\n";
		for (int i = 0; i < calculatedDigest.length; i++) {
			int intVal = calculatedDigest[i];
			if (intVal < 0) {
				intVal += 256;
			}
			retval = retval
					+ String.format("(byte)0x%02X, ",
							new Object[] { Integer.valueOf(intVal) });
			if (i % 8 == 0) {
				retval = retval + "\n";
			}
		}
		retval = retval + "\n};\n";
		return retval;
	}

	public void loadKey(File fin, String userId) throws IOException,
			PGPException {
		loadKey(new FileInputStream(fin), userId);
	}

	@SuppressWarnings("rawtypes")
	public void loadKey(InputStream in, String userId) throws IOException,
			PGPException {
		in = PGPUtil.getDecoderStream(in);

		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in);
		this.key = null;
		Iterator rIt = pgpSec.getKeyRings();
		while ((this.key == null) && (rIt.hasNext())) {
			PGPSecretKeyRing kRing = (PGPSecretKeyRing) rIt.next();
			Iterator kIt = kRing.getSecretKeys();

			while ((this.key == null) && (kIt.hasNext())) {
				PGPSecretKey k = (PGPSecretKey) kIt.next();
				Iterator userIds = k.getUserIDs();
				while (userIds.hasNext()) {
					String keyUserId = (String) userIds.next();
					if (userId == null) {
						if (k.isSigningKey()) {
							this.key = k;
							return;
						}
					} else if ((userId.equals(keyUserId)) && (k.isSigningKey())) {
						this.key = k;
						return;
					}
				}
			}
		}

		if (this.key == null)
			throw new IllegalArgumentException(
					"Can't find signing key in key ring.");
	}

	public String encodeLicense(String keyPassPhraseString) throws IOException,
			NoSuchAlgorithmException, NoSuchProviderException, PGPException,
			SignatureException {
		char[] keyPassPhrase = keyPassPhraseString.toCharArray();
		String licensePlain = getLicenseString();
		ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		OutputStream out = new ArmoredOutputStream(baOut);

		PGPPrivateKey pgpPrivKey = this.key.extractPrivateKey(keyPassPhrase,
				"BC");
		PGPSignatureGenerator sGen = new PGPSignatureGenerator(this.key
				.getPublicKey().getAlgorithm(), this.hashAlgorithm, "BC");

		sGen.initSign(0, pgpPrivKey);

		@SuppressWarnings("rawtypes")
		Iterator it = this.key.getPublicKey().getUserIDs();
		if (it.hasNext()) {
			PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();

			spGen.setSignerUserID(false, (String) it.next());
			sGen.setHashedSubpackets(spGen.generate());
		}

		PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator(2);

		BCPGOutputStream bOut = new BCPGOutputStream(cGen.open(out));

		sGen.generateOnePassVersion(false).encode(bOut);

		PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
		OutputStream lOut = lGen.open(bOut, 'b', "licenseFileName-Ignored",
				new Date(), new byte[1024]);

		InputStream fIn = new ByteArrayInputStream(
				licensePlain.getBytes("utf-8"));

		int ch = 0;
		while ((ch = fIn.read()) >= 0) {
			lOut.write(ch);
			sGen.update((byte) ch);
		}
		lGen.close();
		sGen.generate().encode(bOut);
		cGen.close();
		out.close();
		return new String(baOut.toByteArray());
	}

	public void setLicenseEncoded(String licenseStringEncoded) throws Exception {
		setLicenseEncoded(new ByteArrayInputStream(
				licenseStringEncoded.getBytes("utf-8")));
	}

	public void setLicenseEncodedFromResource(String resourceName)
			throws Exception {
		setLicenseEncoded(PrivateLicense.class.getClassLoader()
				.getResourceAsStream(resourceName));
	}

	public void setLicenseEncodedFromFile(String fileName) throws Exception {
		setLicenseEncoded(new File(fileName));
	}

	public void setLicenseEncodedFromFile(File file) throws Exception {
		setLicenseEncoded(file);
	}

	public void setLicenseEncoded(File file) throws Exception {
		setLicenseEncoded(new FileInputStream(file));
	}

	public Long getDecodeKeyId() {
		return this.decodeKeyId;
	}

	public void setLicenseEncoded(InputStream in) throws Exception {
		ByteArrayInputStream keyIn = new ByteArrayInputStream(
				this.publicKeyRing);
		in = PGPUtil.getDecoderStream(in);

		PGPObjectFactory pgpFact = new PGPObjectFactory(in);
		PGPCompressedData c1 = (PGPCompressedData) pgpFact.nextObject();
		pgpFact = new PGPObjectFactory(c1.getDataStream());
		PGPOnePassSignatureList p1 = (PGPOnePassSignatureList) pgpFact
				.nextObject();

		PGPOnePassSignature ops = p1.get(0);
		PGPLiteralData p2 = (PGPLiteralData) pgpFact.nextObject();
		InputStream dIn = p2.getInputStream();

		PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(
				PGPUtil.getDecoderStream(keyIn));

		this.decodeKeyId = Long.valueOf(ops.getKeyID());
		if (this.decodeKeyId == null) {
			this.verified = false;
			this.licenseProperties = null;
		} else {
			PGPPublicKey decodeKey = pgpRing.getPublicKey(this.decodeKeyId
					.longValue());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				ops.initVerify(decodeKey, "BC");
				int ch;
				while ((ch = dIn.read()) >= 0) {
					ops.update((byte) ch);
					out.write(ch);
				}
				PGPSignatureList p3 = (PGPSignatureList) pgpFact.nextObject();

				if (ops.verify(p3.get(0))) {
					setLicense(new String(out.toByteArray()));
					this.verified = true;
				} else {
					this.verified = false;
					this.licenseProperties = null;
				}
			} catch (Exception e) {
				logger.error("error with encrypted license reading", e);
				this.verified = false;
				this.licenseProperties = null;
			}
		}
	}

	static {
		Security.addProvider(new BouncyCastleProvider());

		logger = Logger.getLogger(PrivateLicense.class);
	}
}