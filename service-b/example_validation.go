package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math"
)

// Example function to demonstrate validation logic
func ExampleValidation() {
	// Example Product Order validation
	productOrderJSON := `{
		"ProductOrder": {
			"GatewayType": "Midtrans",
			"PaymentType": "WLTGopay",
			"CustomerDetail": {
				"Number": "generated-customer-id",
				"FirstName": "John",
				"LastName": "Doe",
				"Email": "john@doe.com",
				"Phone": "081234567890"
			},
			"OrderType": "Product",
			"TransactionDetails": {
				"OrderID": "generated-order-id",
				"GrossAmt": 667000,
				"Currency": "IDR",
				"OrderLevelDiscounts": [
					{
						"Title": "Flash Sale Discount",
						"Amount": -50000,
						"Currency": "IDR"
					}
				]
			},
			"Items": [
				{
					"ProductNumber": "JCK-0001",
					"Name": "Jacket Running",
					"Qty": 1,
					"BasePrice": 200000,
					"Tax": 19000,
					"TotalPrice": 209000,
					"Currency": "IDR",
					"PriceInfo": [
						{
							"Title": "Discounts Raya",
							"Amount": -20000,
							"Currency": "IDR"
						}
					],
					"ShippingDetails": {
						"Cost": 15000
					}
				}
			]
		}
	}`

	var productOrder ProductOrder
	if err := json.Unmarshal([]byte(productOrderJSON), &productOrder); err != nil {
		log.Fatal("Failed to parse product order:", err)
	}

	response := InvoiceValidationResponse{
		IsValid:  true,
		Errors:   []string{},
		Warnings: []string{},
	}

	validateProductOrder(&productOrder, &response)

	fmt.Println("Product Order Validation Result:")
	fmt.Printf("Is Valid: %t\n", response.IsValid)
	fmt.Printf("Order Type: %s\n", response.OrderType)
	fmt.Printf("Order ID: %s\n", response.OrderID)
	fmt.Printf("Calculated Total: %.2f\n", response.Summary.CalculatedTotal)
	fmt.Printf("Declared Total: %.2f\n", response.Summary.DeclaredTotal)
	fmt.Printf("Total Tax: %.2f\n", response.Summary.TotalTax)
	fmt.Printf("Total Discounts: %.2f\n", response.Summary.TotalDiscounts)

	if len(response.Errors) > 0 {
		fmt.Println("Errors:")
		for _, err := range response.Errors {
			fmt.Printf("  - %s\n", err)
		}
	}

	if len(response.Warnings) > 0 {
		fmt.Println("Warnings:")
		for _, warning := range response.Warnings {
			fmt.Printf("  - %s\n", warning)
		}
	}
}

// Example calculation breakdown
func ExampleCalculationBreakdown() {
	fmt.Println("\n=== Calculation Breakdown Example ===")

	// Item 1: Jacket Running
	basePrice := 200000.0
	qty := 1
	itemDiscount := -20000.0
	tax := 19000.0
	shipping := 15000.0

	itemTotal := basePrice*float64(qty) + itemDiscount + tax + shipping
	fmt.Printf("Item 1 (Jacket Running):\n")
	fmt.Printf("  Base Price: %.2f\n", basePrice)
	fmt.Printf("  Quantity: %d\n", qty)
	fmt.Printf("  Discount: %.2f\n", itemDiscount)
	fmt.Printf("  Tax: %.2f\n", tax)
	fmt.Printf("  Shipping: %.2f\n", shipping)
	fmt.Printf("  Item Total: %.2f\n", itemTotal)

	// Order-level discount
	orderDiscount := -50000.0
	fmt.Printf("\nOrder-level Discount: %.2f\n", orderDiscount)

	// Final total
	finalTotal := itemTotal + orderDiscount
	fmt.Printf("Final Total: %.2f\n", finalTotal)
	fmt.Printf("Declared Total: 667000.00\n")
	fmt.Printf("Match: %t\n", math.Abs(finalTotal-667000) < 0.01)
}

// Example with validation errors
func ExampleWithErrors() {
	fmt.Println("\n=== Example with Validation Errors ===")

	// Simulate an order with calculation errors
	productOrderJSON := `{
		"ProductOrder": {
			"TransactionDetails": {
				"OrderID": "error-order",
				"GrossAmt": 100000,
				"Currency": "IDR"
			},
			"Items": [
				{
					"Name": "Test Item",
					"Qty": 1,
					"BasePrice": 50000,
					"Tax": 5000,
					"TotalPrice": 60000,
					"Currency": "IDR",
					"ShippingDetails": {
						"Cost": 10000
					}
				}
			]
		}
	}`

	var productOrder ProductOrder
	if err := json.Unmarshal([]byte(productOrderJSON), &productOrder); err != nil {
		log.Fatal("Failed to parse product order:", err)
	}

	response := InvoiceValidationResponse{
		IsValid:  true,
		Errors:   []string{},
		Warnings: []string{},
	}

	validateProductOrder(&productOrder, &response)

	fmt.Printf("Is Valid: %t\n", response.IsValid)
	fmt.Printf("Calculated Total: %.2f\n", response.Summary.CalculatedTotal)
	fmt.Printf("Declared Total: %.2f\n", response.Summary.DeclaredTotal)

	if len(response.Errors) > 0 {
		fmt.Println("Errors:")
		for _, err := range response.Errors {
			fmt.Printf("  - %s\n", err)
		}
	}
}
