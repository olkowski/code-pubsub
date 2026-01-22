package br.com.fiap;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/email")
@Produces(MediaType.APPLICATION_JSON)
public class Email {

    @Inject
    EnvioEmailService emailService;

    @POST
    @Path("/send")
    public Response publishMessage() {
        try {
            emailService.enviarEmail("rodolfotec@gmail.com", "Assunto teste", "teste body");
            return Response.ok("{\"status\": \"OK\"}").build();
        } catch (Exception e) {
            Log.errorf(e, "Erro ao enviar email");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

}
