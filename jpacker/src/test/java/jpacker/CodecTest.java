package jpacker;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

public class CodecTest {
	@Test
	public void testCodec(){
		try{
			byte[] bys = Base64.decodeBase64("MTM1Njk4MzY2NDU");
			System.out.println(new String(bys));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void testClassName(){
		System.out.println(CodecTest.class.getSimpleName());
	}
	
	@Test
	public void testHashCode(){
		System.out.println("word hello".hashCode());
	}
}
