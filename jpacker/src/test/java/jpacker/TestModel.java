package jpacker;

import java.util.Date;
import java.util.List;

import jpacker.annotation.Column;
import jpacker.annotation.Id;
import jpacker.annotation.RefSelect;
import jpacker.annotation.Select;
import jpacker.annotation.Table;

@Table(name="users")
public class TestModel {
	private Integer id;
	private String name;
	private String password;
	private String password2;
	private String realname;
	private String status;
	private int role;
	private int maxId;
	private Date regtime;
	
	private List<TestModel> testArray;
	
	public Date getRegtime() {
		return regtime;
	}

	public void setRegtime(Date regtime) {
		this.regtime = regtime;
	}

	@Select(targetType=TestModel.class, sql = "select * from users where id < ?",refProperties={"id"},offset=0,limit=1)
	public List<TestModel> getTestArray(){
		return testArray;
	}
	
	public void setTestArray(List<TestModel> test){
		this.testArray = test;
	}
	
	@Select(name="123",sql = "select max(id),min(password) from users",lazy=false)
	public int getMaxId(){
		return maxId;
	}
	
	public void setMaxId(int max){
		this.maxId = max;
	}
	
	@Id
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Column(name="username")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@RefSelect(columnIndex = 2, ref = "123")
	public String getPassword2() {
		return password2;
	}
	public void setPassword2(String password2) {
		this.password2 = password2;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		this.realname = realname;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getRole() {
		return role;
	}
	public void setRole(int role) {
		this.role = role;
	}
	
	public String toString(){
		return id+", "+name+", "+realname+", "+password+", "+status+", "+role+", maxId:"+maxId+",password2:"+password2+",regtime:"+regtime;
	}
	
}
