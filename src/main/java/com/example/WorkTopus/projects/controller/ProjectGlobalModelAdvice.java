import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@ControllerAdvice
@RequiredArgsConstructor
public class ProjectGlobalModelAdvice {

    private final ProjectService projectService;

    @ModelAttribute
    public void addProjectInfo(
            @PathVariable(required = false) Long projectId,
            Model model
    ) {
        if (projectId == null) {
            return;
        }

        Projects project = projectService.findById(projectId);

        model.addAttribute("projectId", projectId);
        model.addAttribute("projectName", project.getName());
    }
}