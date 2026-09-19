import { useEffect, useState } from "react";

import {
  createHouseholdObject,
  deleteHouseholdObject,
  getHouseholdObjects,
  updateHouseholdObject,
  type HouseholdObjectResponse,
} from "../services/householdObjectService";

type HouseholdObjectSectionProps = {
  propertyId: number;
  selectedRoomId: number | null;
  selectedObjectId: number | null;
  onSelectObject: (objectId: number | null) => void;
};

function HouseholdObjectSection({
  propertyId,
  selectedRoomId,
  selectedObjectId,
  onSelectObject,
}: HouseholdObjectSectionProps) {
  const [householdObjects, setHouseholdObjects] =
    useState<HouseholdObjectResponse[]>([]);

  const [objectName, setObjectName] =
    useState("");

  const [objectDescription, setObjectDescription] =
    useState("");

  const [objectManufacturer, setObjectManufacturer] =
    useState("");

  const [objectModel, setObjectModel] =
    useState("");

  const [objectPurchaseDate, setObjectPurchaseDate] =
    useState("");

  // Bearbeitung
  const [editingObjectId, setEditingObjectId] =
    useState<number | null>(null);

  const [editingObjectName, setEditingObjectName] =
    useState("");

  const [
    editingObjectDescription,
    setEditingObjectDescription,
  ] = useState("");

  const [
    editingObjectManufacturer,
    setEditingObjectManufacturer,
  ] = useState("");

  const [
    editingObjectModel,
    setEditingObjectModel,
  ] = useState("");

  const [
    editingObjectPurchaseDate,
    setEditingObjectPurchaseDate,
  ] = useState("");

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  useEffect(() => {
    if (selectedRoomId === null) {
      setHouseholdObjects([]);
      return;
    }

    const loadHouseholdObjects = async () => {
      setError("");

      try {
        const loadedObjects =
          await getHouseholdObjects(
            propertyId,
            selectedRoomId
          );

        setHouseholdObjects(
          loadedObjects
        );
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

    loadHouseholdObjects();
  }, [propertyId, selectedRoomId]);

  const handleCreateHouseholdObject = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (selectedRoomId === null) {
      setError(
        "Bitte zuerst einen Raum auswählen."
      );
      return;
    }

    setError("");
    setMessage("");

    try {
      const createdObject =
        await createHouseholdObject(
          propertyId,
          selectedRoomId,
          {
            name: objectName,
            description:
              objectDescription,
            manufacturer:
              objectManufacturer,
            model:
              objectModel,
            purchaseDate:
              objectPurchaseDate ||
              undefined,
          }
        );

      setHouseholdObjects(
        (currentObjects) => [
          ...currentObjects,
          createdObject,
        ]
      );

      setObjectName("");
      setObjectDescription("");
      setObjectManufacturer("");
      setObjectModel("");
      setObjectPurchaseDate("");

      setMessage(
        "Objekt wurde erfolgreich angelegt."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleEditHouseholdObject = (
    householdObject: HouseholdObjectResponse
  ) => {
    setEditingObjectId(
      householdObject.id
    );

    setEditingObjectName(
      householdObject.name
    );

    setEditingObjectDescription(
      householdObject.description ?? ""
    );

    setEditingObjectManufacturer(
      householdObject.manufacturer ?? ""
    );

    setEditingObjectModel(
      householdObject.model ?? ""
    );

    setEditingObjectPurchaseDate(
      householdObject.purchaseDate ?? ""
    );
  };

  const handleUpdateHouseholdObject =
    async (objectId: number) => {
      if (selectedRoomId === null) {
        return;
      }

      setError("");
      setMessage("");

      try {
        const updatedObject =
          await updateHouseholdObject(
            propertyId,
            selectedRoomId,
            objectId,
            {
              name:
                editingObjectName,

              description:
                editingObjectDescription,

              manufacturer:
                editingObjectManufacturer,

              model:
                editingObjectModel,

              purchaseDate:
                editingObjectPurchaseDate ||
                undefined,
            }
          );

        setHouseholdObjects(
          (currentObjects) =>
            currentObjects.map(
              (householdObject) =>
                householdObject.id ===
                objectId
                  ? updatedObject
                  : householdObject
            )
        );

        setEditingObjectId(null);

        setEditingObjectName("");
        setEditingObjectDescription("");
        setEditingObjectManufacturer("");
        setEditingObjectModel("");
        setEditingObjectPurchaseDate("");

        setMessage(
          "Objekt wurde erfolgreich aktualisiert."
        );
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

  const handleDeleteHouseholdObject =
    async (objectId: number) => {
      if (selectedRoomId === null) {
        return;
      }

      const confirmed =
        window.confirm(
          "Möchtest du dieses Objekt wirklich löschen?"
        );

      if (!confirmed) {
        return;
      }

      setError("");
      setMessage("");

      try {
        await deleteHouseholdObject(
          propertyId,
          selectedRoomId,
          objectId
        );

        setHouseholdObjects(
          (currentObjects) =>
            currentObjects.filter(
              (householdObject) =>
                householdObject.id !==
                objectId
            )
        );

        if (
          selectedObjectId ===
          objectId
        ) {
          onSelectObject(null);
        }

        setMessage(
          "Objekt wurde erfolgreich gelöscht."
        );
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

  if (selectedRoomId === null) {
    return (
      <section>
        <h2>Objekte</h2>

        <p>
          Bitte zuerst einen Raum
          auswählen.
        </p>
      </section>
    );
  }

  return (
    <section>
      <h2>Objekte</h2>

      {error && <p>{error}</p>}
      {message && <p>{message}</p>}

      {householdObjects.length === 0 ? (
        <p>
          Noch keine Objekte in diesem
          Raum vorhanden.
        </p>
      ) : (
        <>
          <h3>Vorhandene Objekte</h3>

          {householdObjects.map(
            (householdObject) => (
              <div
                key={
                  householdObject.id
                }
              >
                {editingObjectId ===
                householdObject.id ? (
                  <>
                    <div>
                      <label>
                        Name
                      </label>

                      <input
                        type="text"
                        value={
                          editingObjectName
                        }
                        onChange={(
                          event
                        ) =>
                          setEditingObjectName(
                            event.target
                              .value
                          )
                        }
                      />
                    </div>

                    <div>
                      <label>
                        Beschreibung
                      </label>

                      <input
                        type="text"
                        value={
                          editingObjectDescription
                        }
                        onChange={(
                          event
                        ) =>
                          setEditingObjectDescription(
                            event.target
                              .value
                          )
                        }
                      />
                    </div>

                    <div>
                      <label>
                        Hersteller
                      </label>

                      <input
                        type="text"
                        value={
                          editingObjectManufacturer
                        }
                        onChange={(
                          event
                        ) =>
                          setEditingObjectManufacturer(
                            event.target
                              .value
                          )
                        }
                      />
                    </div>

                    <div>
                      <label>
                        Modell
                      </label>

                      <input
                        type="text"
                        value={
                          editingObjectModel
                        }
                        onChange={(
                          event
                        ) =>
                          setEditingObjectModel(
                            event.target
                              .value
                          )
                        }
                      />
                    </div>

                    <div>
                      <label>
                        Kaufdatum
                      </label>

                      <input
                        type="date"
                        value={
                          editingObjectPurchaseDate
                        }
                        onChange={(
                          event
                        ) =>
                          setEditingObjectPurchaseDate(
                            event.target
                              .value
                          )
                        }
                      />
                    </div>

                    <button
                      type="button"
                      onClick={() =>
                        handleUpdateHouseholdObject(
                          householdObject.id
                        )
                      }
                    >
                      Speichern
                    </button>

                    <button
                      type="button"
                      onClick={() => {
                        setEditingObjectId(
                          null
                        );
                      }}
                    >
                      Abbrechen
                    </button>
                  </>
                ) : (
                  <>
                    <p>
                      <strong>
                        {
                          householdObject.name
                        }
                      </strong>
                    </p>

                    <p>
                      Beschreibung:{" "}
                      {householdObject.description ||
                        "-"}
                    </p>

                    <p>
                      Hersteller:{" "}
                      {householdObject.manufacturer ||
                        "-"}
                    </p>

                    <p>
                      Modell:{" "}
                      {householdObject.model ||
                        "-"}
                    </p>

                    <p>
                      Kaufdatum:{" "}
                      {householdObject.purchaseDate ||
                        "-"}
                    </p>

                    <button
                      type="button"
                      onClick={() =>
                        handleEditHouseholdObject(
                          householdObject
                        )
                      }
                    >
                      Bearbeiten
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        handleDeleteHouseholdObject(
                          householdObject.id
                        )
                      }
                    >
                      Löschen
                    </button>
                  </>
                )}

                <hr />
              </div>
            )
          )}
        </>
      )}

      <h3>
        Objekt für Wartung auswählen
      </h3>

      <select
        value={selectedObjectId ?? ""}
        onChange={(event) => {
          const value =
            event.target.value;

          if (value === "") {
            onSelectObject(null);
            return;
          }

          onSelectObject(
            Number(value)
          );
        }}
      >
        <option value="">
          Objekt auswählen
        </option>

        {householdObjects.map(
          (householdObject) => (
            <option
              key={
                householdObject.id
              }
              value={
                householdObject.id
              }
            >
              {householdObject.name}
            </option>
          )
        )}
      </select>

      <h3>Neues Objekt anlegen</h3>

      <form
        onSubmit={
          handleCreateHouseholdObject
        }
      >
        <div>
          <label htmlFor="objectName">
            Name
          </label>

          <input
            id="objectName"
            type="text"
            value={objectName}
            onChange={(event) =>
              setObjectName(
                event.target.value
              )
            }
            required
          />
        </div>

        <div>
          <label htmlFor="objectDescription">
            Beschreibung
          </label>

          <input
            id="objectDescription"
            type="text"
            value={objectDescription}
            onChange={(event) =>
              setObjectDescription(
                event.target.value
              )
            }
          />
        </div>

        <div>
          <label htmlFor="objectManufacturer">
            Hersteller
          </label>

          <input
            id="objectManufacturer"
            type="text"
            value={objectManufacturer}
            onChange={(event) =>
              setObjectManufacturer(
                event.target.value
              )
            }
          />
        </div>

        <div>
          <label htmlFor="objectModel">
            Modell
          </label>

          <input
            id="objectModel"
            type="text"
            value={objectModel}
            onChange={(event) =>
              setObjectModel(
                event.target.value
              )
            }
          />
        </div>

        <div>
          <label htmlFor="objectPurchaseDate">
            Kaufdatum
          </label>

          <input
            id="objectPurchaseDate"
            type="date"
            value={objectPurchaseDate}
            onChange={(event) =>
              setObjectPurchaseDate(
                event.target.value
              )
            }
          />
        </div>

        <button type="submit">
          Objekt anlegen
        </button>
      </form>
    </section>
  );
}

export default HouseholdObjectSection;