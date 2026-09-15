package stepDefinitions

import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import io.cucumber.java.en.Then
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling as FailureHandling

class LoginAddToCartCheckoutSteps {

    @Given("user is on the SauceDemo login page")
    def userOnLoginPage() {
        WebUI.openBrowser('')
        WebUI.navigateToUrl('https://www.saucedemo.com')
        WebUI.waitForElementVisible(findTestObject('LoginPage/Page_Swag Labs/input_Swag Labs_user-name'), 10)
    }

    @When("user logs in with username {string} and password {string}")
    def userLogsIn(String username, String password) {
        WebUI.setText(findTestObject('LoginPage/Page_Swag Labs/input_Swag Labs_user-name'), username)
        WebUI.setText(findTestObject('LoginPage/Page_Swag Labs/input_Swag Labs_password'), password)
        WebUI.click(findTestObject('LoginPage/Page_Swag Labs/input_Swag Labs_login-button'))
    }

    @Then("user should see the products page")
    def userSeesProductsPage() {
        WebUI.waitForElementVisible(findTestObject('ProductsPage/Page_Swag Labs/span_Products'), 10)
    }

    @Then("user should see an error message {string}")
    def userSeesErrorMessage(String expectedMessage) {
        String actualMessage
        if (WebUI.verifyElementPresent(findTestObject('LoginPage/Page_Swag Labs/h3_Epic sadface_error_message'), 5, FailureHandling.OPTIONAL)) {
            actualMessage = WebUI.getText(findTestObject('LoginPage/Page_Swag Labs/h3_Epic sadface_error_message'))
        } else {
            actualMessage = WebUI.getText(findTestObject('CheckoutPage/Page_Swag Labs/h3_ErrorMessage_CheckOut'))
        }
        assert actualMessage.contains(expectedMessage)
    }

    @When("user adds {string} to the cart")
    def userAddsItemToCart(String itemName) {

        WebUI.waitForElementClickable(findTestObject('ProductsPage/Page_Swag Labs/div_' + itemName), 10)
        WebUI.click(findTestObject('ProductsPage/Page_Swag Labs/div_' + itemName))

        WebUI.waitForElementClickable(findTestObject('ProductsPage/Page_Swag Labs/button_Add to cart'), 10)
        WebUI.click(findTestObject('ProductsPage/Page_Swag Labs/button_Add to cart'))

        WebUI.waitForElementClickable(findTestObject('ProductsPage/Page_Swag Labs/button_Back to products'), 10)
        WebUI.click(findTestObject('ProductsPage/Page_Swag Labs/button_Back to products'))
        WebUI.waitForElementVisible(findTestObject('ProductsPage/Page_Swag Labs/span_Products'), 10)
    }

    @When("user proceeds to checkout")
    def userProceedsToCheckout() {
        WebUI.click(findTestObject('ProductsPage/Page_Swag Labs/a_Swag Labs_shopping_cart_link'))
        WebUI.waitForElementVisible(findTestObject('CartPage/Page_Swag Labs/button_Checkout'), 10)
        WebUI.click(findTestObject('CartPage/Page_Swag Labs/button_Checkout'))
    }

    @When("user fills in checkout information with first name {string}, last name {string}, and postal code {string}")
    def userFillsCheckoutInfo(String firstName, String lastName, String postalCode) {
        WebUI.setText(findTestObject('CheckoutPage/Page_Swag Labs/input_Checkout Your Information_first-name'), firstName)
        WebUI.setText(findTestObject('CheckoutPage/Page_Swag Labs/input_Checkout Your Information_last-name'), lastName)
        WebUI.setText(findTestObject('CheckoutPage/Page_Swag Labs/input_Checkout Your Information_postal-code'), postalCode)
    }

    @When("user completes the checkout")
    def userCompletesCheckout() {
        WebUI.click(findTestObject('CheckoutPage/Page_Swag Labs/input_Cancel_continue'))
        if (WebUI.verifyElementPresent(findTestObject('CheckoutPage/Page_Swag Labs/button_Finish'), 5, FailureHandling.OPTIONAL)) {
            WebUI.click(findTestObject('CheckoutPage/Page_Swag Labs/button_Finish'))
        } else {
            println("Checkout tidak lanjut ke halaman Finish - kemungkinan validasi form gagal")
        }
    }

    @Then("user should see the order confirmation message {string}")
    def userSeesOrderConfirmation(String expectedMessage) {
        WebUI.waitForElementVisible(findTestObject('CheckoutPage/Page_Swag Labs/h2_Thank you for your order'), 10)
        String actualMessage = WebUI.getText(findTestObject('CheckoutPage/Page_Swag Labs/h2_Thank you for your order'))
        assert actualMessage.contains(expectedMessage.replace("!", ""))
    }

    @When("user goes to the cart page without adding any item")
    def userGoesToEmptyCart() {
        WebUI.click(findTestObject('ProductsPage/Page_Swag Labs/a_Swag Labs_shopping_cart_link'))
        if (WebUI.verifyElementPresent(findTestObject('CartPage/Page_Swag Labs/button_Checkout'), 5, FailureHandling.OPTIONAL)) {
            println("Checkout button is present even with empty cart")
        } else {
            println("Checkout button not present - as expected for empty cart")
        }
    }

    @Then("user should not be able to complete the checkout")
    def userCannotCompleteCheckout() {
        boolean stillOnCartPage = WebUI.verifyElementPresent(findTestObject('CartPage/Page_Swag Labs/button_Checkout'), 5, FailureHandling.OPTIONAL)
        assert stillOnCartPage
    }
}