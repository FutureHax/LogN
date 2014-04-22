curl -X POST \
  -H "X-Parse-Application-Id: FwJZFLtycJHlcbybHzXDPWysNkEk23fDvjlBBteK" \
  -H "X-Parse-Master-Key: SqfzzNez6XnGtQT1y8bg8Za2d7Ft2qVDJGhYww8N" \
  -H "Content-Type: application/json" \
  -d '{"code":"'"$1"'", "sponsor":"'"$2"'"}' \
  https://api.parse.com/1/classes/VerificationCode
