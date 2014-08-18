package jpacker;

import java.sql.SQLException;

import org.junit.Test;

public class ThrowTest {

	@Test
	public void throwTest() {
		try{
			System.out.println("throw Exception");
			throw new Exception("aaaa");
		}catch(Exception e){
			try {
				throw new SQLException(e);
			} catch (SQLException e1) {
				System.out.println("eeeee");
				e1.printStackTrace();
				
			}finally{
				System.out.println("dddddd");
			}
			
		}finally{
			System.out.println("finally");
		}
	}
}
