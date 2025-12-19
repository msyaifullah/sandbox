package main

// AirportData represents an airport with search-relevant information
type AirportData struct {
	Code        string `json:"code"`
	Name        string `json:"name"`
	City        string `json:"city"`
	Country     string `json:"country"`
	CountryCode string `json:"country_code"`
	IsMajor     bool   `json:"is_major"`
	Region      string `json:"region"`
}

// Airport database - Mock data for Malaysian and ASEAN airports
var airportDatabase = []AirportData{
	// Malaysia airports
	{Code: "KUL", Name: "Kuala Lumpur International Airport", City: "Kuala Lumpur", Country: "Malaysia", CountryCode: "MY", IsMajor: true, Region: "Malaysia"},
	{Code: "SZB", Name: "Sultan Abdul Aziz Shah Airport", City: "Subang", Country: "Malaysia", CountryCode: "MY", IsMajor: false, Region: "Malaysia"},
	{Code: "PEN", Name: "Penang International Airport", City: "Penang", Country: "Malaysia", CountryCode: "MY", IsMajor: true, Region: "Malaysia"},
	{Code: "JHB", Name: "Senai International Airport", City: "Johor Bahru", Country: "Malaysia", CountryCode: "MY", IsMajor: true, Region: "Malaysia"},
	{Code: "KBR", Name: "Sultan Ismail Petra Airport", City: "Kota Bharu", Country: "Malaysia", CountryCode: "MY", IsMajor: false, Region: "Malaysia"},
	{Code: "KCH", Name: "Kuching International Airport", City: "Kuching", Country: "Malaysia", CountryCode: "MY", IsMajor: true, Region: "Malaysia"},
	{Code: "BKI", Name: "Kota Kinabalu International Airport", City: "Kota Kinabalu", Country: "Malaysia", CountryCode: "MY", IsMajor: true, Region: "Malaysia"},
	{Code: "LGK", Name: "Langkawi International Airport", City: "Langkawi", Country: "Malaysia", CountryCode: "MY", IsMajor: true, Region: "Malaysia"},
	{Code: "IPH", Name: "Sultan Azlan Shah Airport", City: "Ipoh", Country: "Malaysia", CountryCode: "MY", IsMajor: false, Region: "Malaysia"},
	{Code: "TGG", Name: "Sultan Mahmud Airport", City: "Kuala Terengganu", Country: "Malaysia", CountryCode: "MY", IsMajor: false, Region: "Malaysia"},
	{Code: "AOR", Name: "Sultan Abdul Halim Airport", City: "Alor Setar", Country: "Malaysia", CountryCode: "MY", IsMajor: false, Region: "Malaysia"},
	{Code: "MYY", Name: "Miri Airport", City: "Miri", Country: "Malaysia", CountryCode: "MY", IsMajor: false, Region: "Malaysia"},

	// Singapore
	{Code: "SIN", Name: "Singapore Changi Airport", City: "Singapore", Country: "Singapore", CountryCode: "SG", IsMajor: true, Region: "ASEAN"},

	// Thailand
	{Code: "BKK", Name: "Suvarnabhumi Airport", City: "Bangkok", Country: "Thailand", CountryCode: "TH", IsMajor: true, Region: "ASEAN"},
	{Code: "DMK", Name: "Don Mueang International Airport", City: "Bangkok", Country: "Thailand", CountryCode: "TH", IsMajor: true, Region: "ASEAN"},
	{Code: "HKT", Name: "Phuket International Airport", City: "Phuket", Country: "Thailand", CountryCode: "TH", IsMajor: true, Region: "ASEAN"},
	{Code: "CNX", Name: "Chiang Mai International Airport", City: "Chiang Mai", Country: "Thailand", CountryCode: "TH", IsMajor: true, Region: "ASEAN"},
	{Code: "HDY", Name: "Hat Yai International Airport", City: "Hat Yai", Country: "Thailand", CountryCode: "TH", IsMajor: false, Region: "ASEAN"},

	// Indonesia
	{Code: "CGK", Name: "Soekarno-Hatta International Airport", City: "Jakarta", Country: "Indonesia", CountryCode: "ID", IsMajor: true, Region: "ASEAN"},
	{Code: "DPS", Name: "Ngurah Rai International Airport", City: "Denpasar (Bali)", Country: "Indonesia", CountryCode: "ID", IsMajor: true, Region: "ASEAN"},
	{Code: "SUB", Name: "Juanda International Airport", City: "Surabaya", Country: "Indonesia", CountryCode: "ID", IsMajor: true, Region: "ASEAN"},
	{Code: "UPG", Name: "Sultan Hasanuddin International Airport", City: "Makassar", Country: "Indonesia", CountryCode: "ID", IsMajor: false, Region: "ASEAN"},
	{Code: "BTH", Name: "Hang Nadim International Airport", City: "Batam", Country: "Indonesia", CountryCode: "ID", IsMajor: false, Region: "ASEAN"},

	// Vietnam
	{Code: "HAN", Name: "Noi Bai International Airport", City: "Hanoi", Country: "Vietnam", CountryCode: "VN", IsMajor: true, Region: "ASEAN"},
	{Code: "SGN", Name: "Tan Son Nhat International Airport", City: "Ho Chi Minh City", Country: "Vietnam", CountryCode: "VN", IsMajor: true, Region: "ASEAN"},
	{Code: "DAD", Name: "Da Nang International Airport", City: "Da Nang", Country: "Vietnam", CountryCode: "VN", IsMajor: true, Region: "ASEAN"},

	// Philippines
	{Code: "MNL", Name: "Ninoy Aquino International Airport", City: "Manila", Country: "Philippines", CountryCode: "PH", IsMajor: true, Region: "ASEAN"},
	{Code: "CEB", Name: "Mactan-Cebu International Airport", City: "Cebu", Country: "Philippines", CountryCode: "PH", IsMajor: true, Region: "ASEAN"},
	{Code: "CRK", Name: "Clark International Airport", City: "Angeles City", Country: "Philippines", CountryCode: "PH", IsMajor: false, Region: "ASEAN"},

	// Myanmar
	{Code: "RGN", Name: "Yangon International Airport", City: "Yangon", Country: "Myanmar", CountryCode: "MM", IsMajor: true, Region: "ASEAN"},

	// Cambodia
	{Code: "PNH", Name: "Phnom Penh International Airport", City: "Phnom Penh", Country: "Cambodia", CountryCode: "KH", IsMajor: true, Region: "ASEAN"},
	{Code: "REP", Name: "Siem Reap International Airport", City: "Siem Reap", Country: "Cambodia", CountryCode: "KH", IsMajor: false, Region: "ASEAN"},

	// Laos
	{Code: "VTE", Name: "Wattay International Airport", City: "Vientiane", Country: "Laos", CountryCode: "LA", IsMajor: true, Region: "ASEAN"},

	// Brunei
	{Code: "BWN", Name: "Brunei International Airport", City: "Bandar Seri Begawan", Country: "Brunei", CountryCode: "BN", IsMajor: true, Region: "ASEAN"},
}

// GetAllAirports returns all airports in the database
func GetAllAirports() []AirportData {
	return airportDatabase
}

// DestinationRule defines a rule for filtering available destinations
type DestinationRule struct {
	Description      string
	OriginCondition  func(*AirportData) bool
	DestCondition    func(*AirportData, *AirportData) bool
	Priority         int // Lower number = higher priority
}

// Destination filtering rules
var destinationRules = []DestinationRule{
	{
		Description: "Malaysia to Malaysia - all domestic airports",
		Priority:    1,
		OriginCondition: func(origin *AirportData) bool {
			return origin.CountryCode == "MY"
		},
		DestCondition: func(origin *AirportData, dest *AirportData) bool {
			return dest.CountryCode == "MY"
		},
	},
	{
		Description: "Malaysia to ASEAN - only major airports",
		Priority:    2,
		OriginCondition: func(origin *AirportData) bool {
			return origin.CountryCode == "MY"
		},
		DestCondition: func(origin *AirportData, dest *AirportData) bool {
			return dest.Region == "ASEAN" && dest.IsMajor
		},
	},
	{
		Description: "Singapore to Malaysia - all Malaysian airports",
		Priority:    1,
		OriginCondition: func(origin *AirportData) bool {
			return origin.CountryCode == "SG"
		},
		DestCondition: func(origin *AirportData, dest *AirportData) bool {
			return dest.CountryCode == "MY"
		},
	},
	{
		Description: "Singapore to ASEAN - only major airports",
		Priority:    2,
		OriginCondition: func(origin *AirportData) bool {
			return origin.CountryCode == "SG"
		},
		DestCondition: func(origin *AirportData, dest *AirportData) bool {
			return dest.Region == "ASEAN" && dest.IsMajor
		},
	},
	{
		Description: "ASEAN countries - domestic routes (all airports in same country)",
		Priority:    1,
		OriginCondition: func(origin *AirportData) bool {
			return origin.Region == "ASEAN"
		},
		DestCondition: func(origin *AirportData, dest *AirportData) bool {
			return dest.CountryCode == origin.CountryCode
		},
	},
	{
		Description: "ASEAN countries - international routes (only major airports)",
		Priority:    2,
		OriginCondition: func(origin *AirportData) bool {
			return origin.Region == "ASEAN"
		},
		DestCondition: func(origin *AirportData, dest *AirportData) bool {
			return dest.IsMajor && dest.CountryCode != origin.CountryCode
		},
	},
}

// GetAvailableDestinations returns available destinations based on origin
func GetAvailableDestinations(originCode string) []AirportData {
	// Find the origin airport
	var origin *AirportData
	for _, airport := range airportDatabase {
		if airport.Code == originCode {
			origin = &airport
			break
		}
	}

	// If origin not found, return empty list
	if origin == nil {
		return []AirportData{}
	}

	destinations := []AirportData{}
	includedCodes := make(map[string]bool) // Track already included airports to avoid duplicates

	// Apply filtering rules based on origin
	for _, airport := range airportDatabase {
		// Skip the origin airport itself
		if airport.Code == origin.Code {
			continue
		}

		// Skip if already included
		if includedCodes[airport.Code] {
			continue
		}

		// Check each rule
		for _, rule := range destinationRules {
			// Check if rule applies to this origin
			if !rule.OriginCondition(origin) {
				continue
			}

			// Check if destination matches the rule's condition
			if rule.DestCondition(origin, &airport) {
				destinations = append(destinations, airport)
				includedCodes[airport.Code] = true
				break // Stop checking other rules for this airport
			}
		}
	}

	return destinations
}

// FilterAirportsByKeyword filters a list of airports by keyword
func FilterAirportsByKeyword(airports []AirportData, keyword string) []AirportData {
	if keyword == "" {
		return airports
	}

	results := []AirportData{}
	keywordLower := toLowerString(keyword)

	for _, airport := range airports {
		if contains(toLowerString(airport.Code), keywordLower) ||
			contains(toLowerString(airport.Name), keywordLower) ||
			contains(toLowerString(airport.City), keywordLower) ||
			contains(toLowerString(airport.Country), keywordLower) {
			results = append(results, airport)
		}
	}

	return results
}

// SearchAirports searches for airports by name, city, or code
func SearchAirports(query string) []AirportData {
	return FilterAirportsByKeyword(airportDatabase, query)
}

// Helper function to convert string to lowercase
func toLowerString(s string) string {
	runes := []rune(s)
	for i, r := range runes {
		if r >= 'A' && r <= 'Z' {
			runes[i] = r + 32 // Convert uppercase to lowercase
		}
	}
	return string(runes)
}

// Helper function to check if string contains substring (case-insensitive)
func contains(s, substr string) bool {
	if len(substr) > len(s) {
		return false
	}

	for i := 0; i <= len(s)-len(substr); i++ {
		match := true
		for j := 0; j < len(substr); j++ {
			if s[i+j] != substr[j] {
				match = false
				break
			}
		}
		if match {
			return true
		}
	}
	return false
}
