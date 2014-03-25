Sequel.migration do
  up do
    create_table(:clients) do
      primary_key :id
      String :name, :size => 200

      DateTime :created_at
      DateTime :updated_at
    end
  end

  down do
    drop_table(:clients)
  end
end
