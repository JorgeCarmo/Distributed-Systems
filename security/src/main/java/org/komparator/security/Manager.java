package org.komparator.security;


public class Manager {

    private final static String CA_CERTIFICATE = "ca.cer";
    private final static String CERTIFICATE = "A61_Mediator";
    private static String certificateName;

    private static String keystore;
	private final static String KEYSTORE_PASSWORD = "Rf5rbsQU";

    private static String keyAlias;
	private final static String KEY_PASSWORD = "Rf5rbsQU";
    // Singleton -------------------------------------------------------------

	/* Private constructor prevents instantiation from other classes */
	private Manager() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final Manager INSTANCE = new Manager();
	}

    public static synchronized Manager getInstance() {
		return SingletonHolder.INSTANCE;
	}

    public void setVersion(int i){

    }

    public void setKeystore(String newKeystore){
        keystore = newKeystore;
    }

    public String getKeystore(){
        return keystore;
    }

    public String getKeystorePassword(){
        return KEYSTORE_PASSWORD;
    }

    public void setKeyAlias(String newKeyAlias){
        keyAlias = newKeyAlias;
    }

    public String getKeyAlias(){
        return keyAlias;
    }

    public String getKeyPassword(){
        return KEY_PASSWORD;
    }

    public void setCertificate(String newCertificate){
        certificateName = newCertificate;
    }

    public String getCertificateName(){
        return certificateName;
    }

    public String getCACertificate(){
        return CA_CERTIFICATE;
    }

    public String getCertificate(){
        return CERTIFICATE;
    }

}
