package cn.lightina.managebooks.controller;

import cn.lightina.managebooks.pojo.*;
import cn.lightina.managebooks.service.BookServiceimpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/managebooks")
public class ReaderController {
    @Autowired
    BookServiceimpl bookServiceimpl;

    @RequestMapping(value="/booklist",
            method = RequestMethod.GET)
    public String listBookList(Model model,HttpServletRequest request){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        List<BookList>list= bookServiceimpl.getlist();
        model.addAttribute("list",list);
        return "user_booklist";
    }

    @RequestMapping(value="/query",
            method = RequestMethod.POST)
    public String listBookListById(Model model,HttpServletRequest request){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        String bname=request.getParameter("bname");
        List<BookList>list= bookServiceimpl.getlistByQuery(bname);
        model.addAttribute("list",list);
        return "user_booklist";
    }

    @RequestMapping(value="/{ISBN}/booklist",
            method = RequestMethod.GET)
    public String listBookListById(
            Model model,
            HttpServletRequest request,
            @PathVariable(value="ISBN")String ISBN,
            HttpServletResponse response){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        response.setContentType("text/html;charset=utf8");
        ReservationResult<Reservation> rr;
        PrintWriter pw=null;
        Reservation r=null;
        try {
            pw=response.getWriter();
            r = bookServiceimpl.processRes(ISBN,user);
            rr=new ReservationResult<>(true,r);
        }catch (Exception e){
            rr=new ReservationResult<>(false,"预约失败");
        }
        if(rr.isSuccess()){
            pw.print("<script>alert('预约成功,您的预约号为: "+r.getReservationId()+"');window.location.href='/managebooks/booklist';</script>");
        }else{
            pw.print("<script>alert('预约失败,请重新预约!');window.location.href='/managebooks/booklist';</script>");
        }
        List<BookList>list= bookServiceimpl.getlist();
        model.addAttribute("list",list);
        return "user_booklist";
    }

    // TODO: 2018/5/12 czh
    @RequestMapping(value = "/reservation",
                    method = RequestMethod.GET)
    public String listResListById(Model model,
                                  HttpServletRequest request){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        List<ReservationDetail>list= bookServiceimpl.getResById(user);
        model.addAttribute("list",list);
        return "user_reservation";
    }

    @RequestMapping(value = "/borrow",
                    method = RequestMethod.GET)
    public String listBorListById(Model model,
                                  HttpServletRequest request){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        List<BorrowDetail>list= bookServiceimpl.getBorInfo(user);
        model.addAttribute("list",list);
        return "user_borrow";
    }

    // TODO: 2018/5/13 czh 还书
    @RequestMapping(value = "{borrowId}/return",
            method = RequestMethod.GET)
    public String returnBookById(
            Model model,
            HttpServletRequest request,
            @PathVariable(value = "borrowId")Integer borrowId){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        // TODO: 2018/5/13 czc  book_id对应的书归还入库——修改book.state为1——在reservation表中设置对应记录的deadline 您的触发器接口
        List<BorrowDetail>list= bookServiceimpl.getBorInfo(user);
        model.addAttribute("list",list);
        return "user_borrow";
    }

    /*admin*/
    @RequestMapping(value="/admin/books",
            method = RequestMethod.GET)
    public String showBook(Model model,HttpServletRequest request){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        List<BookList>list= bookServiceimpl.getlist();
        model.addAttribute("list",list);
        return "admin_addbook";
    }

    //添加图书
    @RequestMapping(value = "/admin/books",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public AddResult addbook(HttpServletRequest request,
                             @RequestBody BookList bookList){
        User user=(User)request.getSession().getAttribute("user");
        AddResult ar;
        try {
            bookServiceimpl.addBookList(bookList);
            ar=new AddResult(true);
        }catch (Exception e){
            ar=new AddResult(false);
        }
        return ar;
    }

    @RequestMapping(value = "/admin/reservation",
            method = RequestMethod.GET)
    public String processRes(
            Model model,
            HttpServletRequest request){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        List<ReservationDetail>list= bookServiceimpl.getResList();
        model.addAttribute("list",list);
        return "admin_processreservation";
    }

    // TODO: 2018/5/13 处理预约->插入borrow
    @RequestMapping(
            value = "/admin/{reservationId}/borrow",
            method = RequestMethod.GET)
    public String addBorrow(
            Model model,
            HttpServletRequest request,
            @PathVariable(value = "reservationId") Integer reservationId){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        // TODO: 2018/5/13 czc 插入borrow 触发器???

        List<ReservationDetail>list= bookServiceimpl.getResList();
        model.addAttribute("list",list);
        return "admin_processreservation";
    }

    /*查看借阅情况*/
    @RequestMapping(
            value = "/admin/borrow",
            method = RequestMethod.GET)
    public String showBorrow(
            Model model,
            HttpServletRequest request){
        User user=(User)request.getSession().getAttribute("user");
        model.addAttribute("user",user);
        List<BorrowDetail>list= bookServiceimpl.getBorList();
        model.addAttribute("list",list);
        return "admin_borrow";
    }
}
