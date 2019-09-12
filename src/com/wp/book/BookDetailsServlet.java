package com.wp.book;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class BookDetailsServlet
 */
@WebServlet("/BookDetailsServlet")
public class BookDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection con;
	private PreparedStatement psBookDetails;

	@Override
	public void init() throws ServletException {

		try {
			Class.forName(getServletContext().getInitParameter("driver"));
			con = DriverManager.getConnection(getServletContext().getInitParameter("url"),
					getServletContext().getInitParameter("user"), getServletContext().getInitParameter("pwd"));
			psBookDetails = con.prepareStatement("select * from books where book_id=?");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		String code = request.getParameter("code");

		HttpSession session = request.getSession();
		Cookie[] cookies = request.getCookies();
		int count = 1;
		boolean foundCookie = false;
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				if (cookieName.equals(code)) {
					foundCookie = true;
					count = Integer.parseInt(cookie.getValue());
					count++;
					cookie.setValue("" + count);
					response.addCookie(cookie);
				}

			}
		}
		if (!foundCookie) {
			Cookie newCookie = new Cookie("" + code, "1");
			newCookie.setMaxAge(60 * 30);// half hour
			response.addCookie(newCookie);
		}
		PrintWriter out = response.getWriter();
		try {

			psBookDetails.setString(1, code);
			ResultSet rs = psBookDetails.executeQuery();
			out.println("<html>");
			out.println("<html><body>");
			out.println("<h3>Book-Details</h3>");
			out.println("<hr>");
			while (rs.next()) {
				String bcode = rs.getString(1);
				String title = rs.getString(2);
				String subject = rs.getString(3);
				int price = rs.getInt(4);
				if (count > 10) {
					HashMap<String, Integer> updatedPrice = new HashMap<>();
					price = (int) (price * 1.2);
					updatedPrice.put(bcode, price);
					session.setAttribute("updatedPrice", updatedPrice);
				} else if (count > 5) {
					HashMap<String, Integer> updatedPrice = new HashMap<>();
					price = (int) (price * 1.1);
					updatedPrice.put(bcode, price);
					session.setAttribute("updatedPrice", updatedPrice);
				}
				out.println("<table border=2 cellpadding=0 cellspacing=0>");
				out.println("<tr>");
				out.println("<td>BCode</td>");
				out.println("<td>" + bcode + "</td>");
				out.println("</tr>");

				out.println("<tr>");
				out.println("<td>Title</td>");
				out.println("<td>" + title + "</td>");
				out.println("</tr>");

				out.println("<tr>");
				out.println("<td>Subject</td>");
				out.println("<td>" + subject + "</td>");
				out.println("</tr>");

				out.println("<tr>");
				out.println("<td>Price</td>");
				out.println("<td>" + price + "</td>");
				out.println("</tr>");
				out.println("</table>");
			}
			out.println("<hr>");
			out.println("<a href=CartManager?code=" + code + ">Add-To-Cart</a><br>");
			out.println("<a href='ExploreStore.jsp'>Subject-Page</a><br>");
			out.println("<a href=buyerpage.jsp>Buyer-Page</a><br>");
			out.println("</body></html>");

		} catch (Exception e) {
			out.println(e);
		}
	}

}
