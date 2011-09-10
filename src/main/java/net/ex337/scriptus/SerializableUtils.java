package net.ex337.scriptus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializableUtils {

	public static byte[] serialiseObject(Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		baos.close();
		return baos.toByteArray();
	}

	public static Object deserialiseObject(byte[] b) throws IOException,
			ClassNotFoundException {

		if (b.length == 0)
			return null;

		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object result = ois.readObject();
		ois.close();
		bais.close();
		return result;
	}

}
