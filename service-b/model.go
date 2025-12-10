package main

// Common reusable structures
type Address struct {
	FirstName   string `json:"FirstName"`
	LastName    string `json:"LastName"`
	Phone       string `json:"Phone"`
	Address     string `json:"Address"`
	City        string `json:"City"`
	Postcode    string `json:"Postcode"`
	CountryCode string `json:"CountryCode"`
}

type Contact struct {
	Email   string `json:"Email"`
	Phone   string `json:"Phone"`
	Address string `json:"Address"`
}

type ProductInfo struct {
	Title string `json:"Title"`
	Value string `json:"Value"`
}

type PriceInfo struct {
	Title    string  `json:"Title"`
	Amount   float64 `json:"Amount"`
	Currency string  `json:"Currency"`
}

type BaseOrder struct {
	GatewayType        string             `json:"GatewayType"`
	PaymentType        string             `json:"PaymentType"`
	CustomerDetail     CustomerDetail     `json:"CustomerDetail"`
	OrderType          string             `json:"OrderType"`
	Message            string             `json:"Message"`
	Locale             string             `json:"Locale"`
	TransactionDetails TransactionDetails `json:"TransactionDetails"`
}

type CustomerDetail struct {
	Number          string   `json:"Number"`
	FirstName       string   `json:"FirstName"`
	LastName        string   `json:"LastName"`
	Email           string   `json:"Email"`
	Phone           string   `json:"Phone"`
	BillingAddress  Address  `json:"BillingAddress"`
	ShippingAddress *Address `json:"ShippingAddress,omitempty"`
}

type TransactionDetails struct {
	OrderID             string  `json:"OrderID"`
	GrossAmt            float64 `json:"GrossAmt"`
	Currency            string  `json:"Currency,omitempty"`
	OrderLevelDiscounts []struct {
		Title    string  `json:"Title"`
		Amount   float64 `json:"Amount"`
		Currency string  `json:"Currency"`
		Type     string  `json:"Type"`
		Value    float64 `json:"Value"`
	} `json:"OrderLevelDiscounts,omitempty"`
}

type BaseItem struct {
	BasePrice  float64 `json:"BasePrice"`
	Tax        float64 `json:"Tax"`
	TotalPrice float64 `json:"TotalPrice"`
	Currency   string  `json:"Currency"`
}

type GuestCapacity struct {
	Adults   int `json:"Adults"`
	Children int `json:"Children"`
	Infants  int `json:"Infants"`
}

type Airport struct {
	AirportCode string `json:"AirportCode"`
	City        string `json:"City"`
	Terminal    string `json:"Terminal"`
	Gate        string `json:"Gate"`
}

type FlightSchedule struct {
	DepartureTime string `json:"DepartureTime"`
	ArrivalTime   string `json:"ArrivalTime"`
}

type ServiceSchedule struct {
	CheckInTime       string `json:"CheckInTime"`
	CheckOutTime      string `json:"CheckOutTime"`
	EstimatedDuration string `json:"EstimatedDuration"`
}

type Coordinates struct {
	Latitude  float64 `json:"Latitude"`
	Longitude float64 `json:"Longitude"`
}

type Discount struct {
	Type   string  `json:"Type"`
	Value  float64 `json:"Value"`
	Reason string  `json:"Reason"`
}

type PaymentSchedule struct {
	Type             string  `json:"Type"`
	AdvancePayment   float64 `json:"AdvancePayment"`
	RemainingPayment float64 `json:"RemainingPayment"`
}

type CancellationPolicy struct {
	FreeCancellationHours int `json:"FreeCancellationHours"`
	PartialRefundHours    int `json:"PartialRefundHours"`
	NoRefundHours         int `json:"NoRefundHours"`
}

type Requirements struct {
	CustomerPreparation []string `json:"CustomerPreparation"`
	ProviderEquipment   []string `json:"ProviderEquipment"`
	SpecialInstructions string   `json:"SpecialInstructions"`
}

type EmergencyContact struct {
	Name         string `json:"Name"`
	Phone        string `json:"Phone"`
	Relationship string `json:"Relationship"`
}

type GuestInfo struct {
	GuestId string `json:"GuestId"`
	Name    string `json:"Name"`
}

type PassengerInfo struct {
	PassengerId   string `json:"PassengerId"`
	Name          string `json:"Name"`
	TicketNumber  string `json:"TicketNumber"`
	PassengerType string `json:"PassengerType"`
	DateOfBirth   string `json:"DateOfBirth"`
}

type PassengerSegmentInfo struct {
	SegmentId   string        `json:"SegmentId"`
	PassengerId string        `json:"PassengerId"`
	Seat        string        `json:"Seat"`
	SeatType    string        `json:"SeatType"`
	ProductInfo []ProductInfo `json:"ProductInfo,omitempty"`
}

type PriceBreakdown struct {
	SegmentId   string  `json:"SegmentId"`
	Amount      float64 `json:"Amount"`
	Description string  `json:"Description"`
}

type PassengerPricing struct {
	PassengerId    string           `json:"PassengerId"`
	BasePrice      float64          `json:"BasePrice"`
	Tax            float64          `json:"Tax"`
	TotalPrice     float64          `json:"TotalPrice"`
	Currency       string           `json:"Currency"`
	PriceBreakdown []PriceBreakdown `json:"PriceBreakdown"`
}

type FlightInfo struct {
	ConnectionId     string         `json:"ConnectionId"`
	SegmentId        string         `json:"SegmentId"`
	FlightNumber     string         `json:"FlightNumber"`
	AircraftType     string         `json:"AircraftType"`
	DepartureAirport Airport        `json:"DepartureAirport"`
	ArrivalAirport   Airport        `json:"ArrivalAirport"`
	FlightSchedule   FlightSchedule `json:"FlightSchedule"`
	TravelClass      string         `json:"TravelClass"`
}

type MerchantDiscount struct {
	MerchantID   string `json:"MerchantID"`
	MerchantName string `json:"MerchantName"`
	Discounts    []struct {
		Title    string  `json:"Title"`
		Amount   float64 `json:"Amount"`
		Currency string  `json:"Currency"`
	} `json:"Discounts"`
}

type MerchantSummary struct {
	MerchantID   string  `json:"MerchantID"`
	MerchantName string  `json:"MerchantName"`
	ItemCount    int     `json:"ItemCount"`
	Subtotal     float64 `json:"Subtotal"`
	ShippingCost float64 `json:"ShippingCost"`
	Tax          float64 `json:"Tax"`
	Discounts    float64 `json:"Discounts"`
	TotalAmount  float64 `json:"TotalAmount"`
}

// Product Order Structures
type ProductOrder struct {
	BaseOrder
	Items        []ProductItem `json:"Items"`
	OrderSummary *OrderSummary `json:"OrderSummary,omitempty"`
}

type ProductItem struct {
	ProductNumber   string        `json:"ProductNumber"`
	MerchantID      string        `json:"MerchantID"`
	MerchantName    string        `json:"MerchantName"`
	MerchantContact Contact       `json:"MerchantContact"`
	Name            string        `json:"Name"`
	Qty             int           `json:"Qty"`
	Brand           string        `json:"Brand"`
	Category        string        `json:"Category"`
	ProductInfo     []ProductInfo `json:"ProductInfo"`
	PriceInfo       []PriceInfo   `json:"PriceInfo"`
	BaseItem
	ShippingDetails struct {
		Method         string  `json:"Method"`
		Cost           float64 `json:"Cost"`
		EstimatedDays  string  `json:"EstimatedDays"`
		TrackingNumber *string `json:"TrackingNumber"`
	} `json:"ShippingDetails"`
}

type OrderSummary struct {
	TotalItems        int                `json:"TotalItems"`
	TotalMerchants    int                `json:"TotalMerchants"`
	Subtotal          float64            `json:"Subtotal"`
	TotalShipping     float64            `json:"TotalShipping"`
	TotalTax          float64            `json:"TotalTax"`
	TotalDiscounts    float64            `json:"TotalDiscounts"`
	GrandTotal        float64            `json:"GrandTotal"`
	MerchantDiscounts []MerchantDiscount `json:"MerchantDiscounts"`
	MerchantSummary   []MerchantSummary  `json:"MerchantSummary"`
}

// Service Order Structures
type ServiceOrder struct {
	BaseOrder
	OrderStatus      string            `json:"OrderStatus"`
	ServiceProviders []ServiceProvider `json:"ServiceProviders"`
	Items            []ServiceItem     `json:"Items"`
}

type ServiceProvider struct {
	ProviderID     string   `json:"ProviderID"`
	Name           string   `json:"Name"`
	Contact        Contact  `json:"Contact"`
	Rating         float64  `json:"Rating"`
	Specialization []string `json:"Specialization"`
}

type ServiceItem struct {
	ItemID       string `json:"ItemID"`
	MerchantName string `json:"MerchantName"`
	ProviderID   string `json:"ProviderID"`
	Name         string `json:"Name"`
	Category     string `json:"Category"`
	SubCategory  string `json:"SubCategory"`
	Status       string `json:"Status"`
	Priority     string `json:"Priority"`
	Description  string `json:"Description"`
	ServiceInfo  []struct {
		ServiceType     string          `json:"ServiceType"`
		Duration        int             `json:"Duration"`
		ServiceSchedule ServiceSchedule `json:"ServiceSchedule"`
	} `json:"ServiceInfo"`
	Location struct {
		Address     string      `json:"Address"`
		City        string      `json:"City"`
		Postcode    string      `json:"Postcode"`
		CountryCode string      `json:"CountryCode"`
		Coordinates Coordinates `json:"Coordinates"`
		AccessNotes string      `json:"AccessNotes"`
	} `json:"Location"`
	ProductInfo []struct {
		Title    string `json:"Title"`
		Value    string `json:"Value"`
		Category string `json:"Category"`
		AddonID  string `json:"AddonID"`
		Required bool   `json:"Required"`
		Selected bool   `json:"Selected"`
	} `json:"ProductInfo"`
	PriceInfo []struct {
		Title    string  `json:"Title"`
		Amount   float64 `json:"Amount"`
		Currency string  `json:"Currency"`
		AddonID  string  `json:"AddonID"`
	} `json:"PriceInfo"`
	BaseItem
	Discount           Discount           `json:"Discount"`
	PaymentSchedule    PaymentSchedule    `json:"PaymentSchedule"`
	Requirements       Requirements       `json:"Requirements"`
	CancellationPolicy CancellationPolicy `json:"CancellationPolicy"`
}

// Reservation Order Structures
type ReservationOrder struct {
	BaseOrder
	BookingStatus      string `json:"BookingStatus"`
	CancellationPolicy struct {
		Refundable           bool    `json:"Refundable"`
		CancellationDeadline string  `json:"CancellationDeadline"`
		RefundPercentage     float64 `json:"RefundPercentage"`
	} `json:"CancellationPolicy"`
	SpecialRequests  []string          `json:"SpecialRequests"`
	EmergencyContact EmergencyContact  `json:"EmergencyContact"`
	Items            []ReservationItem `json:"Items"`
}

type ReservationItem struct {
	ReservationNumber string      `json:"ReservationNumber"`
	MerchantName      string      `json:"MerchantName"`
	ReservationType   string      `json:"ReservationType"`
	GuestInfo         []GuestInfo `json:"GuestInfo"`
	ReservationInfo   []struct {
		RoomType     string `json:"RoomType,omitempty"`
		TableType    string `json:"TableType,omitempty"`
		VenueType    string `json:"VenueType,omitempty"`
		ActivityType string `json:"ActivityType,omitempty"`
		StaySchedule *struct {
			CheckInDateTime  string `json:"CheckInDateTime"`
			CheckOutDateTime string `json:"CheckOutDateTime"`
			Duration         int    `json:"Duration"`
		} `json:"StaySchedule,omitempty"`
		ReservationSchedule *struct {
			ReservationDateTime string `json:"ReservationDateTime,omitempty"`
			StartDateTime       string `json:"StartDateTime,omitempty"`
			EndDateTime         string `json:"EndDateTime,omitempty"`
			Duration            int    `json:"Duration"`
		} `json:"ReservationSchedule,omitempty"`
		SeatingCapacity *int `json:"SeatingCapacity,omitempty"`
		VenueCapacity   *int `json:"VenueCapacity,omitempty"`
		Location        *struct {
			Address      string `json:"Address"`
			MeetingPoint string `json:"MeetingPoint"`
		} `json:"Location,omitempty"`
	} `json:"ReservationInfo"`
	ProductInfo []ProductInfo `json:"ProductInfo"`
	PriceInfo   []PriceInfo   `json:"PriceInfo"`
	BaseItem
	RoomCapacity        *GuestCapacity `json:"RoomCapacity,omitempty"`
	TableCapacity       *GuestCapacity `json:"TableCapacity,omitempty"`
	ExpectedGuests      *GuestCapacity `json:"ExpectedGuests,omitempty"`
	ParticipantCapacity *GuestCapacity `json:"ParticipantCapacity,omitempty"`
}

// Airlines Order Structures
type AirlinesOrder struct {
	BaseOrder
	Items []AirlinesItem `json:"Items"`
}

type AirlinesItem struct {
	PNRNumber            string                 `json:"PNRNumber"`
	JourneyType          string                 `json:"JourneyType"`
	PassengerInfo        []PassengerInfo        `json:"PassengerInfo"`
	FlightInfo           []FlightInfo           `json:"FlightInfo"`
	PassengerSegmentInfo []PassengerSegmentInfo `json:"PassengerSegmentInfo"`
	PassengerPricing     []PassengerPricing     `json:"PassengerPricing"`
	PriceInfo            []struct {
		Title       string  `json:"Title"`
		Amount      float64 `json:"Amount"`
		Currency    string  `json:"Currency"`
		SegmentId   string  `json:"SegmentId,omitempty"`
		PassengerId string  `json:"PassengerId,omitempty"`
	} `json:"PriceInfo"`
	BaseItem
}

// Validation Response Structures
type InvoiceValidationResponse struct {
	IsValid   bool     `json:"is_valid"`
	OrderType string   `json:"order_type"`
	OrderID   string   `json:"order_id"`
	Errors    []string `json:"errors,omitempty"`
	Warnings  []string `json:"warnings,omitempty"`
	Summary   struct {
		CalculatedTotal float64 `json:"calculated_total"`
		DeclaredTotal   float64 `json:"declared_total"`
		TotalTax        float64 `json:"total_tax"`
		TotalDiscounts  float64 `json:"total_discounts"`
		Currency        string  `json:"currency"`
		// New detailed breakdown fields
		SubtotalBeforeTaxAndDiscounts float64           `json:"subtotal_before_tax_and_discounts"`
		SubtotalAfterDiscounts        float64           `json:"subtotal_after_discounts"`
		SubtotalAfterTax              float64           `json:"subtotal_after_tax"`
		ItemBreakdown                 []ItemCalculation `json:"item_breakdown,omitempty"`
	} `json:"summary"`
}

type ItemCalculation struct {
	ItemName                      string  `json:"item_name"`
	ItemIndex                     int     `json:"item_index"`
	BasePrice                     float64 `json:"base_price"`
	Quantity                      int     `json:"quantity"`
	SubtotalBeforeTaxAndDiscounts float64 `json:"subtotal_before_tax_and_discounts"`
	Discounts                     float64 `json:"discounts"`
	SubtotalAfterDiscounts        float64 `json:"subtotal_after_discounts"`
	Tax                           float64 `json:"tax"`
	Shipping                      float64 `json:"shipping,omitempty"`
	Addons                        float64 `json:"addons,omitempty"`
	FinalTotal                    float64 `json:"final_total"`
	DeclaredTotal                 float64 `json:"declared_total"`
	IsValid                       bool    `json:"is_valid"`
}

type FlightResult struct {
	Provider     string  `json:"provider"`
	FlightNumber string  `json:"flight_number"`
	Price        float64 `json:"price"`
	From         string  `json:"from"`
	To           string  `json:"to"`
}

type SearchParams struct {
	From string
	To   string
	Date string
}
