package net.javaguides.employeeservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.departmentservice.exception.ResourceNotFoundException;
import net.javaguides.employeeservice.dto.APIResponseDto;
import net.javaguides.employeeservice.dto.DepartmentDto;
import net.javaguides.employeeservice.dto.EmployeeDto;
import net.javaguides.employeeservice.dto.OrganizationDto;
import net.javaguides.employeeservice.entity.Employee;
import net.javaguides.employeeservice.exception.EmailAlreadyExistsException;
import net.javaguides.employeeservice.mapper.AutoEmployeeMapper;
import net.javaguides.employeeservice.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private EmployeeRepository employeeRepository;

    //private RestTemplate restTemplate;

    private WebClient webClient;

    //private APIClient apiClient;

    @Override
    public EmployeeDto saveEmployee(EmployeeDto employeeDto) {

//        Employee employee = new Employee(
//                employeeDto.getId(),
//                employeeDto.getFirstName(),
//                employeeDto.getLastName(),
//                employeeDto.getEmail()
//        );

        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(employeeDto.getEmail());
        if (optionalEmployee.isPresent()) {
            throw new EmailAlreadyExistsException("Email Already Exists for Employee");
        }

        Employee employee = AutoEmployeeMapper.MAPPER.mapToEmployee(employeeDto);

        Employee savedEmployee = employeeRepository.save(employee);

//        EmployeeDto savedEmployeeDto = new EmployeeDto(
//                savedEmployee.getId(),
//                savedEmployee.getFirstName(),
//                savedEmployee.getLastName(),
//                savedEmployee.getEmail()
//        );

        EmployeeDto savedEmployeeDto = AutoEmployeeMapper.MAPPER.mapToEmployeeDto(savedEmployee);

        return savedEmployeeDto;
    }

    @Override
    @Retry(name = "${spring.application.name}", fallbackMethod = "getDefaultDepartment")
    //@CircuitBreaker(name = "${spring.application.name}", fallbackMethod = "getDefaultDepartment")
    public APIResponseDto getEmployeeById(Long employeeId) {
        log.info("inside getEmployeeById() method");

        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

//      ResponseEntity<DepartmentDto> responseEntity = restTemplate.getForEntity("http://localhost:8080/api/departments/" + employee.getDepartmentCode(),
//        DepartmentDto.class);

        DepartmentDto departmentDto = webClient.get().uri("http://localhost:8080/api/departments/" + employee.getDepartmentCode())
                .retrieve()
                .bodyToMono(DepartmentDto.class)
                .block();

        OrganizationDto organizationDto = webClient.get().uri("http://localhost:8083/api/organizations/" + employee.getOrganizationCode())
                .retrieve()
                .bodyToMono(OrganizationDto.class)
                .block();

//                DepartmentDto departmentDto = apiClient.getDepartment(employee.getDepartmentCode());

        EmployeeDto employeeDto = AutoEmployeeMapper.MAPPER.mapToEmployeeDto(employee);

        APIResponseDto apiResponseDto = new APIResponseDto();
        apiResponseDto.setEmployee(employeeDto);
        apiResponseDto.setDepartment(departmentDto);
        apiResponseDto.setOrganization(organizationDto);

//        EmployeeDto employeeDto = new EmployeeDto(
//                employee.getId(),
//                employee.getFirstName(),
//                employee.getLastName(),
//                employee.getEmail()
//        );

        return apiResponseDto;
    }

    public APIResponseDto getDefaultDepartment(Long employeeId, Exception throwable) {
        log.info("inside getDefaultDepartment() method");

        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

//      ResponseEntity<DepartmentDto> responseEntity = restTemplate.getForEntity("http://localhost:8080/api/departments/" + employee.getDepartmentCode(),
//        DepartmentDto.class);

        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setDepartmentName("R&D Department");
        departmentDto.setDepartmentCode("RD001");
        departmentDto.setDepartmentDescription("Research and Development Department");

//      DepartmentDto departmentDto = apiClient.getDepartment(employee.getDepartmentCode());

        EmployeeDto employeeDto = AutoEmployeeMapper.MAPPER.mapToEmployeeDto(employee);

        APIResponseDto apiResponseDto = new APIResponseDto();
        apiResponseDto.setEmployee(employeeDto);
        apiResponseDto.setDepartment(departmentDto);

//        EmployeeDto employeeDto = new EmployeeDto(
//                employee.getId(),
//                employee.getFirstName(),
//                employee.getLastName(),
//                employee.getEmail()
//        );

        return apiResponseDto;
    }

}
