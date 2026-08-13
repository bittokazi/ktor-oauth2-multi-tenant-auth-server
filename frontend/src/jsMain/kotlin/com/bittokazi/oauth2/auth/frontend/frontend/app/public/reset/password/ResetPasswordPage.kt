package com.bittokazi.oauth2.auth.frontend.frontend.app.public.reset.password

import com.bittokazi.kvision.spa.framework.base.common.ObservableManager
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.components.form.FormButton
import com.bittokazi.kvision.spa.framework.base.components.form.FormControl
import com.bittokazi.kvision.spa.framework.base.components.form.FormTextInput
import com.bittokazi.kvision.spa.framework.base.components.form.buttonComponent
import com.bittokazi.kvision.spa.framework.base.components.form.textInputComponent
import com.bittokazi.kvision.spa.framework.base.services.SpaTenantService.tenantInfoObserver
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.utils.Utils
import io.kvision.core.UNIT
import io.kvision.core.getElementJQuery
import io.kvision.core.onClick
import io.kvision.core.onEvent
import io.kvision.form.form
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

@OptIn(ExperimentalSerializationApi::class)
class ResetPasswordPage(
    token: String,
) : SimplePanel() {
    val resetPasswordForm = ResetPasswordForm()
    private val routing = SpaAppEngine.routing
    private val errorMessage: ObservableValue<String> = ObservableValue("")

    init {
        var tenantName = "AuthKit"

        var errorDiv: Div? = null

        val submitFunc: ((Event) -> Unit) = { ev ->
            ev.preventDefault()
            errorDiv?.content?.let {
                errorMessage.setState("")
            }
            if (!resetPasswordForm.isValid()) {
                errorMessage.setState("Invalid Input")
                resetPasswordForm.enforceValidation()
            } else {
                resetPasswordForm.submitButton.showLoading()
                resetPassword(
                    token,
                    PasswordResetFormRequest(
                        password = resetPasswordForm.password.getValue(),
                        reTypedPassword = resetPasswordForm.reTypedPassword.getValue(),
                    ),
                )
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
                            errorMessage.setState("An error occurred")
                            resetPasswordForm.submitButton.resetInput()
                        }
                    }
            }
        }

        val verifyFunc: (callback: ((Int) -> Unit)) -> Unit = { callback ->
            verifyToken(token).then {
                callback(200)
            }.catch {
                if (it is RemoteRequestException) {
                    if (it.code.toInt() == 404) {
                        callback(404)
                    } else if (it.code.toInt() == 400) {
                        callback(400)
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
                                        p("Please provide your new password to reset", className = "lead")
                                    }

                                    div(className = "card") {
                                        val form =
                                            Div(className = "card-body") {
                                                div(className = "m-sm-3") {
                                                    form {
                                                        ObservableManager.setSubscriber("companyInfoObserverRpPage") {
                                                            tenantInfoObserver.subscribe {
                                                                if (it != null) {
                                                                    div(className = "mb-3") {
                                                                        add(
                                                                            textInputComponent(
                                                                                formTextInput = resetPasswordForm.password,
                                                                                value = "",
                                                                                inputType = InputType.PASSWORD,
                                                                            ),
                                                                        )
                                                                    }
                                                                    div(className = "mb-3") {
                                                                        add(
                                                                            textInputComponent(
                                                                                formTextInput = resetPasswordForm.reTypedPassword,
                                                                                value = "",
                                                                                inputType = InputType.PASSWORD,
                                                                            ),
                                                                        )
                                                                    }
                                                                    div(className = "d-grid gap-2 mt-3") {
                                                                        add(
                                                                            buttonComponent(
                                                                                resetPasswordForm.submitButton,
                                                                                "Submit",
                                                                            ),
                                                                        )
                                                                        resetPasswordForm.submitButton.input.onClick {
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
                                                    add(errorDiv)
                                                    errorDiv.hide()
                                                }
                                            }
                                        add(form)
                                        form.hide()

                                        val errorNotFound =
                                            div(className = "text-center mb-3") {
                                                span("Invalid token", rich = true)
                                            }
                                        add(errorNotFound)
                                        errorNotFound.hide()

                                        val errorExpired =
                                            div(className = "text-center mb-3") {
                                                span("Expired token", rich = true)
                                            }
                                        add(errorExpired)
                                        errorExpired.hide()

                                        verifyFunc {
                                            when (it) {
                                                200 -> form.show()
                                                400 -> errorExpired.show()
                                                404 -> errorNotFound.show()
                                            }
                                            routing.updatePageLinks()
                                        }

                                        div(className = "text-center mb-3") {
                                            span("Want to login to your account?&nbsp;", rich = true)
                                            link("Continue here", "/app/login", dataNavigo = true)
                                        }
                                    }

                                    ObservableManager.setSubscriber("passwordResetPageVersionObserver") {
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
data class PasswordResetFormRequest(
    val password: String,
    val reTypedPassword: String,
)

@Serializable
data class PasswordResetFormRequestResponse(
    val message: String,
)

@Serializable
data class VerifyTokenResponse(
    val message: String,
)

fun verifyToken(token: String): Promise<RestResponse<VerifyTokenResponse>> {
    val restClient = SpaAppEngine.restService.getPublicClient()
    return restClient.request<VerifyTokenResponse>(
        AppEngine.restService.BASE_URL + "/api/v1/users/password-reset/$token",
    ) {
        method = HttpMethod.GET
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun resetPassword(
    token: String,
    passwordResetFormRequest: PasswordResetFormRequest,
): Promise<RestResponse<PasswordResetFormRequestResponse>> {
    val restClient = SpaAppEngine.restService.getPublicClient()
    return restClient.request<PasswordResetFormRequestResponse>(
        AppEngine.restService.BASE_URL + "/api/v1/users/password-reset/$token",
    ) {
        method = HttpMethod.POST
        data = Json.encodeToDynamic(passwordResetFormRequest)
    }
}

class ResetPasswordForm : FormControl<Unit, Unit> {
    val password =
        FormTextInput(
            label = "New Password",
            placeholder = "Enter new password",
            defaultInvalidFeedback = "Password must be minimum 8 characters long",
        ) {

            return@FormTextInput it != null && it.length >= 8
        }

    val reTypedPassword =
        FormTextInput(
            label = "Re Type New Password",
            placeholder = "Re-type new password",
            defaultInvalidFeedback = "Passwords do not match",
        ) {

            return@FormTextInput it != null && it == password.input.value
        }

    val submitButton = FormButton()

    override fun setInput(input: Unit) {}

    override fun getInput() {}

    override fun isValid(): Boolean {
        return password.isValid() && reTypedPassword.isValid()
    }

    override fun setCustomError(message: String) {}

    override fun enforceValidation() {
        password.enforceValidation()
        reTypedPassword.enforceValidation()
    }

    override fun getValue() {}
}
