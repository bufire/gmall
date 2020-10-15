package com.atguigu.gmallindex.control;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmallindex.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Controller
public class IndexController {
    @Autowired
    private IndexService indexService;

    @GetMapping
    public String toIndex(Model model){

        List<CategoryEntity> categoryEntities = this.indexService.queryLvl1Categories();
        model.addAttribute("categories", categoryEntities);

        // TODO: 加载其他数据

        return "index";
    }

    @ResponseBody
    @GetMapping("index/cates/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesWithSub(@PathVariable("pid")Long pid){

        List<CategoryEntity> categoryEntities = this.indexService.queryLvl2CategoriesWithSub(pid);
        return ResponseVo.ok(categoryEntities);
    }

//    @ResponseBody
//    @GetMapping("index/testlock")
//    public ResponseVo<Object> testLock(){
//        indexService.testLock();
//        System.out.println("关");
//        return ResponseVo.ok(null);
//    }
//
//    @ResponseBody
//    @GetMapping("index/read")
//    public ResponseVo<String> read(){
//        String msg = indexService.readLock();
//
//        return ResponseVo.ok(msg);
//    }
//
//    @ResponseBody
//    @GetMapping("index/write")
//    public ResponseVo<String> write(){
//        String msg = indexService.writeLock();
//
//        return ResponseVo.ok(msg);
//    }
//
//    /**
//     * 等待
//     * @return
//     */
//    @ResponseBody
//    @GetMapping("index/latch")
//    public ResponseVo<Object> countDownLatch(){
//
//        String msg = indexService.latch();
//
//        return ResponseVo.ok(msg);
//    }
//
//    /**
//     * 计数
//     * @return
//     */
//    @ResponseBody
//    @GetMapping("index/out")
//    public ResponseVo<Object> out(){
//
//        String msg = indexService.countDown();
//
//        return ResponseVo.ok(msg);
//    }
}
