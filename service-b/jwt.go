package main

import (
	"errors"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

var (
	privateKey        []byte
	serviceAPublicKey []byte
)

func init() {
	privateKey, _ = os.ReadFile("serviceB_private.pem")
	serviceAPublicKey, _ = os.ReadFile("serviceA_public.pem")
}

func createJWT() (string, error) {
	key, err := jwt.ParseRSAPrivateKeyFromPEM(privateKey)
	if err != nil {
		return "", err
	}

	claims := jwt.RegisteredClaims{
		Subject:   "service-b-user",
		Issuer:    "service-b",
		Audience:  []string{"service-a"},
		ExpiresAt: jwt.NewNumericDate(time.Now().Add(30 * time.Minute)),
		IssuedAt:  jwt.NewNumericDate(time.Now()),
	}

	token := jwt.NewWithClaims(jwt.SigningMethodRS256, claims)
	return token.SignedString(key)
}

func verifyToken(tokenString string) (*jwt.RegisteredClaims, error) {
	pubKey, err := jwt.ParseRSAPublicKeyFromPEM(serviceAPublicKey)
	if err != nil {
		return nil, err
	}

	token, err := jwt.ParseWithClaims(tokenString, &jwt.RegisteredClaims{}, func(t *jwt.Token) (interface{}, error) {
		return pubKey, nil
	}, jwt.WithAudience("service-b"), jwt.WithIssuer("service-a"))
	if err != nil {
		return nil, err
	}

	if claims, ok := token.Claims.(*jwt.RegisteredClaims); ok && token.Valid {
		return claims, nil
	}
	return nil, errors.New("invalid token")
}
