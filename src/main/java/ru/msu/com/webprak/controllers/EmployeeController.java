package ru.msu.com.webprak.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.com.webprak.DAO.EmployeeDAO;
import ru.msu.com.webprak.DAO.PositionHistoryDAO;
import ru.msu.com.webprak.DAO.impl.EmployeeDAOImpl;
import ru.msu.com.webprak.DAO.impl.PositionHistoryDAOImpl;
import ru.msu.com.webprak.models.Employee;
import ru.msu.com.webprak.models.PositionHistory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class EmployeeController {
    @Autowired
    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();

    @Autowired
    private final PositionHistoryDAO positionHistoryDAO = new PositionHistoryDAOImpl();

    @GetMapping("/employees")
    public String getEmployees(Model model) {
        List<Employee> employee = (List<Employee>) employeeDAO.getAll();
        model.addAttribute("employee", employee);
        model.addAttribute("employeeDAO", employeeDAO);
        return "employees";
    }

    @GetMapping("/searchEmployee")
    public String searchEmployee(@RequestParam(required = false) Long id,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String position,
                                 @RequestParam(required = false) String projectIds,
                                 @RequestParam(required = false) Integer minLengthOfService,
                                 @RequestParam(required = false) Integer maxLengthOfService,
                                 @RequestParam(required = false) String awardType,
                                 Model model) {

        List<Long> projectIdList = null;
        if (projectIds != null && !projectIds.isEmpty()) {
            projectIdList = Arrays.stream(projectIds.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        if (name != null && name.isEmpty()) {
            name = null;
        }
        if (position != null && position.isEmpty()) {
            position = null;
        }
        if (awardType != null && awardType.isEmpty()) {
            awardType = null;
        }

        EmployeeDAO.Filter filter = EmployeeDAO.getFilterBuilder()
                .id(id)
                .name(name)
                .position(position)
                .projectIds(projectIdList)
                .minLengthOfService(minLengthOfService)
                .maxLengthOfService(maxLengthOfService)
                .awardType(awardType)
                .build();

        List<Employee> employees = employeeDAO.searchEmployees(filter);

        model.addAttribute("employee", employees);
        model.addAttribute("employeeDAO", employeeDAO);
        return "employees";
    }

    @PostMapping("/addEmployee")
    public String addEmployee(@RequestParam String name,
                              @RequestParam String homeAddress,
                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dayOfBirth,
                              @RequestParam String education,
                              @RequestParam String position,
                              @RequestParam Integer lengthOfService,
                              RedirectAttributes redirectAttributes) {
        Employee newEmployee = new Employee(name, homeAddress, dayOfBirth, education, position, lengthOfService);
        Employee savedEmployee = employeeDAO.save(newEmployee);

        PositionHistory newPositionHistory = new PositionHistory(savedEmployee, position, new Date());
        positionHistoryDAO.save(newPositionHistory);

        redirectAttributes.addFlashAttribute("message", "Employee added successfully!");

        return "redirect:/employees";
    }
}
