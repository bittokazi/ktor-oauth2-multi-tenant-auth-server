package com.bittokazi.oauth2.auth.frontend.frontend.app.public.reset.password

import com.bittokazi.kvision.spa.framework.base.common.ObservableManager
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.services.SpaTenantService.tenantInfoObserver
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.utils.Utils
import io.kvision.core.UNIT
import io.kvision.core.getElementJQuery
import io.kvision.core.onClick
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.form.text.TextInput
import io.kvision.html.*
import io.kvision.panel.SimplePanel
import io.kvision.rest.*
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.events.Event
import kotlin.js.Promise

class ResetPasswordRequestPage() : SimplePanel() {
    private val routing = SpaAppEngine.routing
    private val errorMessage: ObservableValue<String> = ObservableValue("")

    init {
        lateinit var email: TextInput
        var tenantName = "AuthKit IDP"

        var errorDiv: Div? = null

        val submitFunc: ((Event) -> Unit) = { ev ->
            ev.preventDefault()
            errorDiv?.content?.let {
                errorMessage.setState("")
            }
            if (email.value == null) {
                errorMessage.setState("Blank Email")
            } else {
                resetRequest(PasswordResetRequest(email.value!!))
                    .then {
                        sweetAlert.fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Success",
                                    "text" to it.data.message,
                                    "icon" to "success",
                                ),
                            ),
                        )
                        routing.navigate("/app/login")
                    }.catch {
                        if (it is RemoteRequestException) {
                            console.log("e -> $it")
                            if (it.code.toInt() == 404) {
                                errorMessage.setState("Email not found")
                            }
                        }
                    }
            }
        }

        errorMessage.subscribe {
            if (errorDiv != null && errorDiv?.visible == false) {
                errorDiv?.show()
                errorDiv?.getElementJQuery()?.hide(0)
            }
            errorDiv?.getElementJQuery()?.slideToggle(300).also { _it ->
                when (it) {
                    "" -> errorDiv?.getElementJQuery()?.fadeOut(300, "linear")
                    else -> {
                        errorDiv?.getElementJQuery()?.fadeIn(300, "linear")
                    }
                }
            }
        }

        when (SpaAppEngine.defaultAuthHolder.getAuth()) {
            null ->
                main(className = "d-flex w-100") {
                    div(className = "container d-flex flex-column") {
                        div(className = "row vh-100") {
                            div(className = "col-sm-10 col-md-8 col-lg-6 col-xl-5 mx-auto d-table h-100") {
                                div(className = "d-table-cell align-middle") {
                                    div(className = "text-center mt-4") {
                                        h1(className = "h2").bind(tenantInfoObserver) {
                                            if (it != null && !it.cpanel) {
                                                tenantName = it.name
                                            }
                                            content = "Reset Password"
                                        }
                                        p("To reset your password please provide your email", className = "lead")
                                    }

                                    div(className = "card") {
                                        div(className = "card-body") {
                                            div(className = "m-sm-3") {
                                                form {
                                                    ObservableManager.setSubscriber("companyInfoObserverRpPage") {
                                                        tenantInfoObserver.subscribe {
                                                            if (it?.cpanel == true) {
                                                                div(className = "mb-3") {
                                                                    label("Email", className = "form-label")
                                                                    email =
                                                                        TextInput(
                                                                            InputType.EMAIL,
                                                                            className = "form-control form-control-lg",
                                                                        ) {
                                                                            placeholder = "Enter your email"
                                                                        }
                                                                    add(email)
                                                                }
                                                                div(className = "d-grid gap-2 mt-3") {
                                                                    button(
                                                                        "Submit",
                                                                        type = ButtonType.BUTTON,
                                                                        className = "btn btn-lg btn-danger",
                                                                    ).onClick {
                                                                        submitFunc(it)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }.onEvent {
                                                    submit = submitFunc
                                                }
                                                errorDiv =
                                                    Div(className = "alert alert-danger align-items-center") {
                                                        marginTop = 10 to UNIT.px
                                                        setAttribute("role", "alert")
                                                        div {
                                                            errorMessage.subscribe {
                                                                content = it
                                                            }
                                                        }
                                                    }
                                                add(errorDiv!!)
                                                errorDiv?.hide()
                                            }
                                        }

                                        div(className = "text-center mb-3") {
                                            span("Want to login to your account?&nbsp;", rich = true)
                                            link("Continue here", "/app/login", dataNavigo = true).toString()
                                        }
                                    }

                                    ObservableManager.setSubscriber("companyInfoObserverVersion") {
                                        tenantInfoObserver.subscribe {
                                            if (it != null) {
                                                div(className = "text-center pb-3") {
                                                    span {
                                                        p {
                                                            span("${it.systemVersion} | ")
                                                            span(
                                                                "Change Log",
                                                                className = "cursor-pointer"
                                                            ).onClick {
                                                                val changeLogs = AppEngine.tenantService.changeLogs.ifEmpty { listOf("No changelog available") }
                                                                sweetAlert.fire(
                                                                    Json.encodeToDynamic(
                                                                        mapOf(
                                                                            "title" to "Change log",
                                                                            "html" to Utils.formatChangeLogHtml(changeLogs),
                                                                            "icon" to "info",
                                                                            "confirmButtonText" to "Close",
                                                                            "width" to "600px"
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            else -> SpaAppEngine.routing.navigate("/app/dashboard")
        }
    }
}

@Serializable
data class PasswordResetRequest(
    val email: String,
)

@Serializable
data class PasswordResetRequestResponse(
    val message: String,
)

@OptIn(ExperimentalSerializationApi::class)
fun resetRequest(passwordResetRequest: PasswordResetRequest): Promise<RestResponse<PasswordResetRequestResponse>> {
    val restClient = RestClient()
    return restClient.request<PasswordResetRequestResponse>(
        AppEngine.restService.BASE_URL + "/api/v1/users/password-reset/request-token",
    ) {
        method = HttpMethod.POST
        data = Json.encodeToDynamic(passwordResetRequest)
    }
}
