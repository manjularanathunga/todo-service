package com.demo.todoservice;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.deploy.net.HttpResponse;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

interface ToDoRepository extends MongoRepository<ToDo, Long> {
    ToDo findById(@Param("id") String id);

    Collection<ToDo> findByTodoName(@Param("todoName") String todoName);

    void deleteById(String id);
}

@SpringBootApplication
public class TodoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoServiceApplication.class, args);
    }
}

@CrossOrigin
@RestController
@RequestMapping("/demo")
class ToDoRestController {
    @Autowired
    private ToDoRepository toDoRepository;

    @RequestMapping("/list-todo")
    public List<ToDo> loadAll() {
        return toDoRepository.findAll();
    }

    @RequestMapping("/delete-todo")
    public Responce deleteTodo(@RequestParam("id") String id) {
        ToDo toDoEntity = toDoRepository.findById(id);
        try {
            if(toDoEntity !=null){
                toDoRepository.delete(toDoEntity);
                return new Responce(HttpServletResponse.SC_OK,"Record "+toDoEntity.getToDoCode()+" Deleted Successfully");
            }else
                return new Responce(HttpServletResponse.SC_BAD_REQUEST,"Record not Found");

        } catch (Exception e) {
            return new Responce(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Deletion Fail for ID "+id);
        }
    }

    @RequestMapping("/save-todo")
    public Responce saveTodo(@RequestParam("entity") String todo) {
        ObjectMapper mapper = new ObjectMapper();
        ToDo toDoEntity = null;
        try {
            toDoEntity = (ToDo) mapper.readValue(todo,ToDo.class);
        } catch (IOException e) {
            return new Responce(HttpServletResponse.SC_BAD_REQUEST,"Bad request, Record not Saved");
        }

        try {
            if(toDoEntity !=null){
                toDoRepository.save(toDoEntity);
                return new Responce(HttpServletResponse.SC_OK,"Record Saved Successfully");
            }else
                return new Responce(HttpServletResponse.SC_BAD_REQUEST,"Bad request, Record not Saved");

        } catch (Exception e) {
            return new Responce(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Record Saved Successfully");
        }
    }

    @RequestMapping("/update-todo")
    public Responce update(@RequestParam("entity") String todo) {
        ObjectMapper mapper = new ObjectMapper();
        ToDo toDoEntity = null;
        try {
            toDoEntity = (ToDo) mapper.readValue(todo,ToDo.class);
        } catch (IOException e) {
            return new Responce(HttpServletResponse.SC_BAD_REQUEST,"Bad request, Record not Saved");
        }

        try {
            if(toDoEntity !=null){
                ToDo dbTodo = toDoRepository.findById(toDoEntity.getId());
                if(!todo.equals(dbTodo)){
                    toDoRepository.save(toDoEntity);
                    return new Responce(HttpServletResponse.SC_OK,"Record updated Successfully");
                }
            }else
                return new Responce(HttpServletResponse.SC_BAD_REQUEST,"Bad request, Record not Saved");

        } catch (Exception e) {
            return new Responce(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Record update Fail");
        }
        return new Responce(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Record update Fail");
    }
}

class ToDo {

    @Id
    private String id;
    private String toDoCode;
    private String todoName;
    private String status;

    public ToDo() {

    }

    public ToDo(String toDoCode, String todoName, String status) {
        this.toDoCode = toDoCode;
        this.todoName = todoName;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToDoCode() {
        return toDoCode;
    }

    public void setToDoCode(String toDoCode) {
        this.toDoCode = toDoCode;
    }

    public String getTodoName() {
        return todoName;
    }

    public void setTodoName(String todoName) {
        this.todoName = todoName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ToDo{" +
                "Id=" + id +
                ", toDoCode='" + toDoCode + '\'' +
                ", todoName='" + todoName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        ToDo tmp=(ToDo) obj;
        return  id.equals(tmp.id) &&  toDoCode.equals(tmp.toDoCode) &&  todoName.equals(tmp.todoName) &&  status.equals(tmp.status);
    }
}

class Responce{

    public Responce(int status, String message) {
        this.status = status;
        this.message = message;
    }

    int status;
    String message;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ResourceException extends RuntimeException {
    public ResourceException() {
        super();
    }
    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }
    public ResourceException(String message) {
        super(message);
    }
    public ResourceException(Throwable cause) {
        super(cause);
    }
}