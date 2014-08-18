package jpacker.processor;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RowUtils {
	
	private static RowProcessor rowProcessor = new BasicRowProcessor(); 
	
	public static void instance(RowProcessor processor){
		rowProcessor =  processor;
	}
	
	public static Object[] toArray(ResultSet rs) throws SQLException {
		return rowProcessor.toArray(rs);
	}

    public static <T> T toBean(ResultSet rs, Class<T> type) throws SQLException{
    	return rowProcessor.toBean(rs, type);
    }
    
    public static <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException{
    	return rowProcessor.toBeanList(rs, type);
    }

    public static Map<String, Object> toMap(ResultSet rs) throws SQLException{
    	return rowProcessor.toMap(rs);
    }
    
	public static <T> T autoConvert(ResultSet rs, Class<T> type) throws SQLException{
		return autoConvert(rs, 1, type);
    }
	
    @SuppressWarnings("unchecked")    
    public static <T> T autoConvert(ResultSet rs,int index, Class<T> type) throws SQLException{
    	 if ( !type.isPrimitive() && rs.getObject(index) == null ) {
             return null;
         }
         if ( !type.isPrimitive() && rs.getObject(index) == null ) {
             return null;
         }
    	
    	if(type == Integer.TYPE || type == Integer.class ){
			return (T)Integer.valueOf(rs.getInt(index));
		}else if(type == Long.TYPE|| type == Long.class){
			return (T)Long.valueOf(rs.getLong(index));
		}else if(type == Double.TYPE || type == Double.class){
			return (T)Double.valueOf(rs.getDouble(index));
		}else if(type == Float.TYPE || type == Float.class){
			return (T)Float.valueOf(rs.getFloat(index));
		}else if(type == BigDecimal.class){
			return type.cast(rs.getBigDecimal(index));
		}else if(type == Byte.TYPE || type == Byte.class){
			return (T)Byte.valueOf(rs.getByte(index));
		}else if(type == Boolean.TYPE || type == Boolean.class){
			return (T)Boolean.valueOf(rs.getBoolean(index));
		}else if(type == Character.class || type == Character.TYPE){
			String cv = rs.getString(index);
        	if(cv != null && cv.length() > 0){
        		return type.cast(cv.charAt(0));
        	}
            return type.cast('\0');
		}else if(type == Byte[].class){
			return type.cast(rs.getBytes(index));
		}else if(type == Object.class){
			return type.cast(rs.getObject(index));
		}else if(type == Map.class){
			return (T)toMap(rs);
		}else if(type == Object[].class){
			return (T)toArray(rs);
		}else if(type == String.class ){
			return type.cast(rs.getString(index));
		}else if(type == Date.class){
			return type.cast(new Date(rs.getTimestamp(index).getTime()));
		}else{
			return null;
		}
    }
}
