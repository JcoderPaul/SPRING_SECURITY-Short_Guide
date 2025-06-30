package me.oldboy.controllers.webui;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.constants.SecurityConstants;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/webui")
@RequiredArgsConstructor
public class LoginRegController {

    @Autowired
    private final ClientService clientService;

    @GetMapping("/login")
    public String getLoginPage(HttpServletResponse response) {

            log.info("Redirect to - client_forms/login.html - from @GetMapping('/login')");

            return "client_forms/login.html";
    }

    @GetMapping("/main")
    public String getMainPage(){
        log.info("Redirect to - /main.html - from @GetMapping(/main)");

        return "/main.html";
    }

    @GetMapping("/registration")
    public String regClientPage(Model model,
                                @ModelAttribute("client") ClientCreateDto clientCreateDto,
                                @ModelAttribute("details") DetailsCreateDto detailsCreateDto) {
        model.addAttribute("client", clientCreateDto);
        model.addAttribute("details", detailsCreateDto);
        return "client_forms/registration.html";
    }

    @PostMapping("/registration")
    public String createClientWebUi(@Validated @ModelAttribute("client") ClientCreateDto clientCreateDto,
                                    BindingResult bindingResultForClient,
                                    @Validated @ModelAttribute("details") DetailsCreateDto detailsCreateDto,
                                    BindingResult bindingResultForDetails,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResultForClient.hasErrors() || bindingResultForDetails.hasErrors()) {
            redirectAttributes.addFlashAttribute("client", clientCreateDto);
            redirectAttributes.addFlashAttribute("details", detailsCreateDto);
            redirectAttributes.addFlashAttribute("errorsClient", bindingResultForClient.getAllErrors());
            redirectAttributes.addFlashAttribute("errorsDetails", bindingResultForDetails.getAllErrors());
            return "redirect:/webui/registration";
        } else {
            clientCreateDto.setDetails(detailsCreateDto);
            clientService.saveClient(clientCreateDto);
            return "redirect:/login";
        }
    }
}
