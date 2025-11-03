package com.ahmedyassin.TravelSmart.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString; // Utile pour le débogage

@Getter
@Setter
@ToString // Pour afficher facilement l'objet dans les logs
public class GeminiTravelPlanDTO {
    private String planName; // Ex: "Plan Économique", "Plan Confort"
    private Double estimatedTotalCost; // Coût total estimé

    // Détails des différents domaines
    private String flightsInfo; // Informations sur les vols
    private String accommodationInfo; // Informations sur l'hébergement (hôtels)
    private String localTransportInfo; // Informations sur les transports locaux
    private String activitiesInfo; // Suggestions d'activités/agenda
    private String visaRequirementsInfo; // Exigences de visa
    private String insuranceOptionsInfo; // Options d'assurance
    private String costBreakdown; // Répartition détaillée des coûts (peut être un JSON string ou texte)

    // Si l'IA peut suggérer plusieurs plans, ce DTO peut être une partie d'un tableau
    // Pour l'instant, on suppose que le champ 'optimizationDetails' contiendra un JSON avec UN plan,
    // ou un tableau si vous changez le type de 'optimizationDetails' en JSONB ou un autre type complexe.
    // Pour la simplicité, on le garde en String et on peut y stocker un seul plan ou un tableau de plans.
}