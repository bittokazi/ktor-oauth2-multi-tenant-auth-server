package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.form

import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Client
import com.bittokazi.kvision.spa.framework.base.components.form.FormButton
import com.bittokazi.kvision.spa.framework.base.components.form.FormControl
import com.bittokazi.kvision.spa.framework.base.components.form.FormSelectInput
import com.bittokazi.kvision.spa.framework.base.components.form.FormSwitchInput
import com.bittokazi.kvision.spa.framework.base.components.form.FormTextInput
import com.bittokazi.kvision.spa.framework.base.components.form.buttonComponent
import com.bittokazi.kvision.spa.framework.base.components.form.selectInputComponent
import com.bittokazi.kvision.spa.framework.base.components.form.switchInputComponent
import com.bittokazi.kvision.spa.framework.base.components.form.textInputComponent
import io.kvision.core.Container
import io.kvision.core.UNIT
import io.kvision.core.getElementJQuery
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import org.w3c.dom.events.Event

fun Container.clientFormComponent(
    update: Boolean = false,
    clientForm: ClientForm,
    client: Client?,
    submitCallback: () -> Unit // Client form doesn't seem to handle file uploads
): Container {

    var errorDiv: Div? = null

    val errorMessage: ObservableValue<String> = ObservableValue("")

    val submitForm: ((Event) -> Unit) = { ev ->
        ev.preventDefault()
        if (errorMessage.value != "") {
            errorMessage.setState("")
        }

        if (!clientForm.isValid()) {
            errorMessage.setState("Invalid Input")
            clientForm.enforceValidation()
        } else {
            clientForm.submitButton.showLoading()
            submitCallback()
        }
    }

    errorMessage.subscribe {
        errorDiv?.getElementJQuery()?.slideToggle()
        when (it) {
            "" -> errorDiv?.getElementJQuery()?.fadeOut(300, "linear")
            else -> {
                errorDiv?.getElementJQuery()?.fadeIn(300, "linear")
            }
        }
    }

    return form {
        div(className = "row") {
            div(className = "col-md-6") {
                add(
                    textInputComponent(
                        clientForm.clientId,
                        client?.clientId ?: "",
                        disabledInput = !update
                    )
                )
            }
            div(className = "col-md-6") {
                add(textInputComponent(clientForm.clientName, client?.clientName ?: ""))
            }
            div(className = "col-md-6") {
                add(textInputComponent(clientForm.scope, client?.scopes?.joinToString(",") ?: ""))
            }
            div(className = "col-md-6") {
                add(
                    textInputComponent(
                        clientForm.authorizedGrantTypes,
                        client?.grantTypes?.joinToString(",") ?: ""
                    )
                )
            }
            div(className = "col-md-6") {
                add(
                    textInputComponent(
                        clientForm.webServerRedirectUri,
                        client?.redirectUris?.joinToString(",") ?: ""
                    )
                )
            }
            div(className = "col-md-6") {
                add(textInputComponent(clientForm.accessTokenValidity, client?.accessTokenValidity?.toString() ?: ""))
            }
            div(className = "col-md-6") {
                add(
                    textInputComponent(
                        clientForm.refreshTokenValidity,
                        client?.refreshTokenValidity?.toString() ?: ""
                    )
                )
            }
            div(className = "col-md-6") {
                add(
                    selectInputComponent(
                        clientForm.clientType,
                        listOf("" to "Please Select", "confidential" to "confidential", "public" to "public"),
                        client?.clientType ?: ""
                    )
                )
            }
            div(className = "col-md-3") {
                add(switchInputComponent(clientForm.requireConsent, client?.consentRequired == true))
            }
            div(className = "col-md-9")
            div(className = "col-md-3") {
                add(
                    switchInputComponent(
                        clientForm.generateSecret,
                        !update,
                        disabledInput = !update
                    )
                )
            }
            div(className = "col-md-10")
            div(className = "col-md-3") {
                add(
                    buttonComponent(
                        clientForm.submitButton,
                        when (update) {
                            true -> "Update"
                            false -> "Add"
                        }
                    )
                )
            }
            div(className = "col-md-9")
            div(className = "col-md-3") {
                window.setTimeout({
                    if (errorDiv == null) {
                        errorDiv = Div(className = "alert alert-danger align-items-center") {
                            marginTop = 10 to UNIT.px
                            setAttribute("role", "alert")
                            div {
                                errorMessage.subscribe {
                                    content = it
                                }
                            }
                        }
                        add(errorDiv!!)
                        errorDiv?.getElementJQuery()?.hide(0)
                    }
                }, 50)
            }
        }
        onEvent {
            submit = submitForm
        }
        window.setTimeout({
            clientForm.initForm()
        }, 100)
    }
}

class ClientForm(update: Boolean) : FormControl<Unit, Unit> {

    val clientId = FormTextInput(
        label = "Client ID",
        placeholder = when (update) {
            true -> "Please enter unique client ID"
            false -> "Will be auto generated"
        },
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput true
    }

    val clientName = FormTextInput(
        label="Client Name",
        placeholder = "Enter client name",
        defaultInvalidFeedback = "a to z and _ is allowed",

    ) {
        if(it == null) {
            return@FormTextInput false
        }

        val regex = "^[a-z_ A-Z0-9]+$".toRegex()
        return@FormTextInput regex.matches(it)
    }

    val scope = FormTextInput(
        label = "Scopes",
        placeholder = "Enter scopes comma(,) separated",
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput it != null
    }

    val authorizedGrantTypes = FormTextInput(
        label = "Authorized Grant Types",
        placeholder = "Enter grant types comma(,) separated",
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput it != null
    }

    val webServerRedirectUri = FormTextInput(
        label = "Redirect URL",
        placeholder = "...",
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput true
    }

    val accessTokenValidity = FormTextInput(
        label = "Access Token Validity",
        placeholder = "In seconds",
        defaultInvalidFeedback = "Only number is allowed"
    ) {
        if(it == null) {
            return@FormTextInput false
        }

        val regex = "^[0-9]+$".toRegex()
        return@FormTextInput regex.matches(it)
    }

    val refreshTokenValidity = FormTextInput(
        label = "Refresh Token Validity",
        placeholder = "In seconds",
        defaultInvalidFeedback = "Only number is allowed"
    ) {
        if(it == null) {
            return@FormTextInput false
        }

        val regex = "^[0-9]+$".toRegex()
        return@FormTextInput regex.matches(it)
    }

    val requireConsent = FormSwitchInput(
        label = "Require User Consent",
        defaultInvalidFeedback = "Invalid Settings"
    ) {
        return@FormSwitchInput it != null
    }

    val generateSecret = FormSwitchInput(
        label = "Generate Secret",
        defaultInvalidFeedback = "Invalid Settings"
    ) {
        return@FormSwitchInput it != null
    }

    val clientType = FormSelectInput(
        label = "Client Type",
        defaultInvalidFeedback = "Select a client type"
    ) {
        it != 0
    }

    val submitButton = FormButton()

    override fun setInput(input: Unit) {}

    override fun getInput() {}

    override fun isValid(): Boolean {
        return clientId.isValid()
                && clientName.isValid()
                && scope.isValid()
                && authorizedGrantTypes.isValid()
                && webServerRedirectUri.isValid()
                && accessTokenValidity.isValid()
                && refreshTokenValidity.isValid()
                && requireConsent.isValid()
                && generateSecret.isValid()
                && clientType.isValid()
    }

    override fun setCustomError(message: String) {}

    override fun enforceValidation() {
        clientId.enforceValidation()
        clientName.enforceValidation()
        scope.enforceValidation()
        authorizedGrantTypes.enforceValidation()
        webServerRedirectUri.enforceValidation()
        accessTokenValidity.enforceValidation()
        refreshTokenValidity.enforceValidation()
        requireConsent.enforceValidation()
        generateSecret.enforceValidation()
        clientType.enforceValidation()
    }

    override fun getValue() {}

    fun initForm() {
        if (clientId.isValid()) clientId.enforceValidation()
        if (clientName.isValid()) clientName.enforceValidation()
        if (scope.isValid()) scope.enforceValidation()
        if (authorizedGrantTypes.isValid()) authorizedGrantTypes.enforceValidation()
        if (webServerRedirectUri.isValid()) webServerRedirectUri.enforceValidation()
        if (accessTokenValidity.isValid()) accessTokenValidity.enforceValidation()
        if (refreshTokenValidity.isValid()) refreshTokenValidity.enforceValidation()
        if (requireConsent.isValid()) requireConsent.enforceValidation()
        if (generateSecret.isValid()) generateSecret.enforceValidation()
        if (clientType.isValid()) clientType.enforceValidation()
    }
}

fun clientFormErrorHandler(
    errors: Map<String, List<String>>?,
    clientForm: ClientForm
) {
    clientForm.submitButton.resetInput()
}
