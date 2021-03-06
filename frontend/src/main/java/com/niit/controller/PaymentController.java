package com.niit.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.niit.onlineshopping.DAO.CartDAO;
import com.niit.onlineshopping.DAO.OrderDAO;
import com.niit.onlineshopping.Model.CartItem;
import com.niit.onlineshopping.Model.OrderDetail;



@Controller
public class PaymentController 
{
	
	@Autowired
	CartDAO cartDAO;
	
	@Autowired
	OrderDAO orderDAO;

	@RequestMapping(value="/orderConfirm{cartItemId}")
	public String showOrderConfirm(@PathVariable("cartItemId") int cartItemId,@RequestParam("qty") int quantity,HttpSession session,Model m)
	{
		CartItem cartItem=cartDAO.getCartItem(cartItemId);
		cartItem.setQuantity(quantity);
		cartDAO.updateCart(cartItem);
		
		String username=(String)session.getAttribute("loggedInUserID");
		List<CartItem> listCartItems=cartDAO.listCartItems(username);
		
		m.addAttribute("listCartItems", listCartItems);
		m.addAttribute("totalCost", this.calculateTotalCost(listCartItems));
		session.setAttribute("CartItems",listCartItems.size());
		
		return "OrderConfirm";
	}
	
	@RequestMapping(value="/paymentConfirmation")
	public String paymentConfirm(@RequestParam("mode")String paymentMode,HttpSession session,Model m)
	{
		String username=(String)session.getAttribute("loggedInUserID");
		List<CartItem> listCartItems=cartDAO.listCartItems(username);
		
		m.addAttribute("listCartItems", listCartItems);
		m.addAttribute("totalCost", this.calculateTotalCost(listCartItems));
		session.setAttribute("CartItems",listCartItems.size());
		
		if(orderDAO.updateCartItemStatus(username))
		{
		OrderDetail orderDetail=new OrderDetail();
		orderDetail.setPurchaseValue(this.calculateTotalCost(listCartItems));
		orderDetail.setPaymentMode(paymentMode);
		orderDetail.setUsername(username);
		orderDetail.setOrderDate(new java.util.Date());
		orderDAO.receiptGenerate(orderDetail);
		
		session.setAttribute("orderId", orderDetail.getOrderId());
		session.setAttribute("orderdate",orderDetail.getOrderDate());
		
		}
		
		return "Receipt";
	}
	
	public int calculateTotalCost(List<CartItem> cartItems)
	{
		int totalCost=0;
		int count=0;
		
		while(count<cartItems.size())
		{
			totalCost=totalCost+(cartItems.get(count).getPrice()*cartItems.get(count).getQuantity());
			count++;
		}
		
		return totalCost;
	}
	
}
