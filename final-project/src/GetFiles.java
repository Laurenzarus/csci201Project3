import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetFiles")
public class GetFiles extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//From previous page, extract parameters
		String email = request.getParameter("email");
		String rawHTML = "";
		
		//Begin database access
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/db?user=root&password=password&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC");
			
			
			
			//Check if email already exists in our database
			ps = conn.prepareStatement("SELECT * FROM Users WHERE email=?");
			ps.setString(1, email);
			rs = ps.executeQuery();
			rs.next();
			int uID = rs.getInt("userID");
			
			//now we have uID and email
			//need to retrieve notes and make a table for the sidebar (1 column)
			
			//First, retrieve fileIDS for user from Access table
			ps2 = conn.prepareStatement("SELECT * FROM Access WHERE userID=?");
			
			ps2.setString(1,  Integer.toString(uID));
			rs2 = ps2.executeQuery();
			
			//Declare list to hold fileIDS
			ArrayList<Integer> fileID = new ArrayList<Integer>();
			ArrayList<Integer> aFileID = new ArrayList<Integer>();
			ArrayList<String> fileName = new ArrayList<String>();
			while (rs2.next()) {
				fileID.add(rs2.getInt("fileID"));
			}
			//Next, retrieve files from DB that match fileIDs
			String statement3 = "SELECT * FROM Files WHERE fileID in ";
			statement3 +="(";
			int i = 0;
			if (fileID.size() > 1) {
				while (i < fileID.size()) {
					statement3+=fileID.get(i) + ",";
					i++;
				}
			}
			else {
				statement3+=fileID.get(0) + ")";
			}
			if (fileID.size() > 0) {
				if (fileID.size() > 1) {
					statement3 +=  fileID.get(i-i) + ");";
				}
				ps3 = conn.prepareStatement(statement3);
				rs3 = ps3.executeQuery();
				while (rs3.next()) {
					aFileID.add(rs3.getInt("fileID"));
					fileName.add(rs3.getString("fileName"));
				}
				//<thead><tr><th scope=\"col\">Filename</th></tr></thead>
				rawHTML+=("<table class=\"table table-bordered table-dark\"><tbody>");
				for (int j = 0; j < fileName.size(); j++) {
	        		//names
		         	rawHTML+=("<tr>");
		         	rawHTML+=("<td onclick=\"loadFile('" + aFileID.get(j) + "')\">"  + fileName.get((j)) +  "</td>");
		         	rawHTML+=("</tr>");
				}
				rawHTML+=("</tbody></table>");
				out.print(rawHTML);
			}
			else {
				//exit
				out.print("Empty");
			}
		} catch(SQLException sqle) {
			System.out.println("sqle: " + sqle.getMessage());
		} catch(ClassNotFoundException cnfe) {
			System.out.println("cnfe: " + cnfe.getMessage());
		} finally {
			try {
				if(conn!=null) {
					conn.close();
				}
				if(ps!=null) {
					ps.close();
				}
				if(ps2!=null) {
					ps2.close();
				}
				if(ps3!=null) {
					ps3.close();
				}
				if(rs!=null) {
					rs.close();
				}
				if(rs2!=null) {
					rs2.close();
				}
				if(rs3!=null) {
					rs3.close();
				}
			} catch (SQLException sqle) {
				System.out.println("sqle closing stream:-" + sqle.getMessage());
			}
		}
	}
}